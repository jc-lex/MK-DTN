package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
}
