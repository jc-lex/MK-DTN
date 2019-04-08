package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeDuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

final class DummyStorage {
    private DummyStorage() {}
    
    // Storage DB queues
    static final List<DTNBundle> OUTBOUND_BUNDLES_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final List<DTNBundle> TRANSMITTED_BUNDLES_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final List<DTNBundle> DELIVERED_FRAGMENTS_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final List<DTNBundle> DELIVERED_BUNDLES_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    // Storage DB tables
    static final String OUTBOUND_BUNDLES_DB = "mkdtn.obdb";
    static final String TRANSMITTED_BUNDLES_DB = "mkdtn.tbdb";
    static final String DELIVERED_BUNDLES_DB = "mkdtn.dbdb";
    static final String DELVIERED_FRAGMENTS_DB = "mkdtn.dfdb";
    static final String NODE_EID_DB = "mkdtn.nedb";
    
    synchronized static boolean write(Serializable obj, FileOutputStream file) {
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(file))) {
            out.writeObject(obj);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    synchronized static Serializable read(FileInputStream file) {
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(file))) {
            return (Serializable) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    synchronized static DTNBundle next() {
        List<DTNBundle> bundleList = new ArrayList<>(OUTBOUND_BUNDLES_QUEUE);
        
        Set<DTNBundle> highestPriorityBundles = new HashSet<>();
        int p = -1;
        do {
            p++;
            if (p > 2) break;
            for (DTNBundle dtnBundle : bundleList) {
                PrimaryBlock.PriorityClass priorityClass
                    = dtnBundle.primaryBlock.priorityClass;
                if (p == 0) {
                    if (priorityClass.equals(PrimaryBlock.PriorityClass.EXPEDITED)) {
                        highestPriorityBundles.add(dtnBundle);
                    }
                } else if (p == 1) {
                    if (priorityClass.equals(PrimaryBlock.PriorityClass.NORMAL)) {
                        highestPriorityBundles.add(dtnBundle);
                    }
                } else if (p == 2){
                    if (priorityClass.equals(PrimaryBlock.PriorityClass.BULK)) {
                        highestPriorityBundles.add(dtnBundle);
                    }
                }
            }
        } while (highestPriorityBundles.isEmpty());
        if (p > 2) return new DTNBundle();
        else if (highestPriorityBundles.size() == 1) {
            DTNBundle[] theOnlyOne = highestPriorityBundles.toArray(new DTNBundle[0]);
            return theOnlyOne[0];
        }
        
        Set<DTNBundle> shortestLivingBundles = new HashSet<>();
        int l = -1;
        do {
            l++;
            if (l > 2) break;
            for (DTNBundle dtnBundle : highestPriorityBundles) {
                DTNTimeDuration lifeTime = dtnBundle.primaryBlock.lifeTime;
                if (l == 0) {
                    if (lifeTime.equals(PrimaryBlock.LifeTime.FIVE_HOURS.getDuration())) {
                        shortestLivingBundles.add(dtnBundle);
                    }
                } else if (l == 1) {
                    if (lifeTime.equals(PrimaryBlock.LifeTime.FIFTEEN_HOURS.getDuration())) {
                        shortestLivingBundles.add(dtnBundle);
                    }
                } else if (l == 2) {
                    if (lifeTime.equals(PrimaryBlock.LifeTime.THIRTY_FIVE_HOURS.getDuration())) {
                        shortestLivingBundles.add(dtnBundle);
                    }
                }/* else if (l == 3) {
                    if (lifeTime.equals(PrimaryBlock.LifeTime.TWO_MONTHS.getDuration())) {
                        shortestLivingBundles.add(dtnBundle);
                    }
                }*/
            }
        } while (shortestLivingBundles.isEmpty());
        if (l > 2) return new DTNBundle();
        else if (shortestLivingBundles.size() == 1) {
            DTNBundle[] theOnlyOne = shortestLivingBundles.toArray(new DTNBundle[0]);
            return theOnlyOne[0];
        }
        
        DTNTimeDuration maximumAge = DTNTimeDuration.ZERO;
        // get maximum age
        for (DTNBundle dtnBundle : shortestLivingBundles) {
            CanonicalBlock CBlock = dtnBundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
            assert CBlock != null;
            AgeBlock ageBlock = (AgeBlock) CBlock.blockTypeSpecificDataFields;
            
            if (ageBlock.age.compareTo(maximumAge) > 0) {
                maximumAge = ageBlock.age;
            }
        }
        for (DTNBundle dtnBundle : shortestLivingBundles) {
            CanonicalBlock CBlock = dtnBundle.canonicalBlocks.get(DTNBundle.CBlockNumber.AGE);
            assert CBlock != null;
            AgeBlock ageBlock = (AgeBlock) CBlock.blockTypeSpecificDataFields;
            
            if (ageBlock.age.equals(maximumAge)) {
                return dtnBundle; // first eldest bundle
            }
        }
        
        return new DTNBundle();
    }

//    synchronized static void clear() {
//        OUTBOUND_BUNDLES_QUEUE.clear();
//        TRANSMITTED_BUNDLES_QUEUE.clear();
//        DELIVERED_BUNDLES_QUEUE.clear();
//        DELIVERED_FRAGMENTS_QUEUE.clear();
//    }
}
