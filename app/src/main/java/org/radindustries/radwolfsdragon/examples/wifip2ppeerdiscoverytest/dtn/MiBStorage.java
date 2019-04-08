package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.util.Log;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.DConstants;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.AgeBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CanonicalBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNEndpointID;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.PrimaryBlock;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.time.DTNTimeDuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

final class MiBStorage {
    private static final String LOG_TAG
        = DConstants.MAIN_LOG_TAG + "_" + MiBStorage.class.getSimpleName();
    
    // flat files
    private static final String OUTBOUND_BUNDLES_DB = "mkdtn.obdb";
    private static final String TRANSMITTED_BUNDLES_DB = "mkdtn.tbdb";
    private static final String DELIVERED_BUNDLES_DB = "mkdtn.dbdb";
    private static final String DELIVERED_FRAGMENTS_DB = "mkdtn.dfdb";
    private static final String NODE_EID_DB = "mkdtn.nedb";
    
    // queues
    static final List<DTNBundle> DBQ = Collections.synchronizedList(new LinkedList<DTNBundle>());
    static final List<DTNBundle> DFQ = Collections.synchronizedList(new LinkedList<DTNBundle>());
    static final List<DTNBundle> TBQ = Collections.synchronizedList(new LinkedList<DTNBundle>());
    static final List<DTNBundle> OBQ = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final int OUTBOUND_BUNDLES_QUEUE = 0;
    static final int DELIVERED_FRAGMENTS_QUEUE = 1;
    static final int DELIVERED_BUNDLES_QUEUE = 2;
    static final int TRANSMITTED_BUNDLES_QUEUE = 3;
    
    private MiBStorage() {}
    
    static synchronized DTNEndpointID getNodeEID(Context context) {
        String eid = readEID(context);
        writeEID(context, eid);
        return DTNEndpointID.parse(eid);
    }
    
    private static synchronized String readEID(Context context) {
        try (Scanner scanner = new Scanner(context.openFileInput(NODE_EID_DB))){
            return scanner.nextLine();
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not read from " + NODE_EID_DB, e);
            return makeEID().toString();
        }
    }
    
    private static synchronized DTNEndpointID makeEID() {
        String eid = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
        return DTNEndpointID.from(DTNEndpointID.DTN_SCHEME, eid.substring(0, 4));
    }
    
    private static synchronized void writeEID(Context context, String eid) {
        try (PrintWriter writer
                 = new PrintWriter(context.openFileOutput(NODE_EID_DB, Context.MODE_PRIVATE))) {
            writer.println(eid);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "could not write to " + NODE_EID_DB, e);
        }
    }
    
    static synchronized void readDB(Context context) {
        readQueue(context, OUTBOUND_BUNDLES_QUEUE);
        readQueue(context, DELIVERED_BUNDLES_QUEUE);
        readQueue(context, DELIVERED_FRAGMENTS_QUEUE);
        readQueue(context, TRANSMITTED_BUNDLES_QUEUE);
    }
    
    static synchronized void writeDB(Context context) {
        writeQueue(context, OUTBOUND_BUNDLES_QUEUE);
        writeQueue(context, DELIVERED_BUNDLES_QUEUE);
        writeQueue(context, DELIVERED_FRAGMENTS_QUEUE);
        writeQueue(context, TRANSMITTED_BUNDLES_QUEUE);
    }
    
    static synchronized void writeQueue(Context c, int q) {
        List<DTNBundle> queue; String fileName;
        switch (q) {
            case OUTBOUND_BUNDLES_QUEUE: queue = OBQ; fileName = OUTBOUND_BUNDLES_DB; break;
            case DELIVERED_BUNDLES_QUEUE: queue = DBQ; fileName = DELIVERED_BUNDLES_DB; break;
            case DELIVERED_FRAGMENTS_QUEUE: queue = DFQ; fileName = DELIVERED_FRAGMENTS_DB; break;
            case TRANSMITTED_BUNDLES_QUEUE: queue = TBQ; fileName = TRANSMITTED_BUNDLES_DB; break;
            default: return;
        }
        
        try (FileOutputStream fos = c.openFileOutput(fileName, Context.MODE_PRIVATE);
             ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(fos))) {
            DTNBundle[] data = queue.toArray(new DTNBundle[0]);
            out.writeObject(data);
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not write to " + fileName, e);
        }
    }
    
    private static synchronized void readQueue(Context c, int q) {
        List<DTNBundle> queue; String fileName;
        switch (q) {
            case OUTBOUND_BUNDLES_QUEUE: queue = OBQ; fileName = OUTBOUND_BUNDLES_DB; break;
            case DELIVERED_BUNDLES_QUEUE: queue = DBQ; fileName = DELIVERED_BUNDLES_DB; break;
            case DELIVERED_FRAGMENTS_QUEUE: queue = DFQ; fileName = DELIVERED_FRAGMENTS_DB; break;
            case TRANSMITTED_BUNDLES_QUEUE: queue = TBQ; fileName = TRANSMITTED_BUNDLES_DB; break;
            default: return;
        }
        
        try (FileInputStream fis = c.openFileInput(fileName);
             ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(fis))) {
            DTNBundle[] data = (DTNBundle[]) in.readObject();
            queue.addAll(Arrays.asList(data));
        } catch (Exception e) {
            Log.e(LOG_TAG, "could not read from " + fileName, e);
        }
    }
    
    static synchronized DTNBundle next() {
        List<DTNBundle> bundleList = new ArrayList<>(OBQ);
    
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
}
