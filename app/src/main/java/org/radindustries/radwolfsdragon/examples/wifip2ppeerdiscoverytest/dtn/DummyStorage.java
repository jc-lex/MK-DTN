package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

final class DummyStorage {
    private DummyStorage() {}
    
    // Storage DB queues
    static final List<DTNBundle> OUTBOUND_BUNDLES_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
//    static final List<DTNBundle> INTERMEDIATE_BUNDLES_QUEUE
//        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final List<DTNBundle> DELIVERED_FRAGMENTS_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
    
    static final List<DTNBundle> DELIVERED_BUNDLES_QUEUE
        = Collections.synchronizedList(new LinkedList<DTNBundle>());
}
