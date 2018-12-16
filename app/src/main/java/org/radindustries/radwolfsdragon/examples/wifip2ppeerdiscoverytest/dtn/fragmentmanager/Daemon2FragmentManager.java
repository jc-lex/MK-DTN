package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface Daemon2FragmentManager {
    int DEFAULT_FRAGMENT_PAYLOAD_SIZE_IN_BYTES = 1024;
    
    DTNBundle[] fragment(DTNBundle bundleToFragment);
    DTNBundle[] fragment(DTNBundle bundleToFragment, int fragmentPayloadSizeInBytes);
    DTNBundle defragment(DTNBundle[] fragmentsToCombine);
    boolean defragmentable(DTNBundle[] fragmentsToCombine);
}
