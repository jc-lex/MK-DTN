package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;

public interface Daemon2FragmentManager {
    int KIBI_BYTE = 1024;
    int DEFAULT_FRAGMENT_PAYLOAD_SIZE_IN_BYTES = 500 * KIBI_BYTE;
    String[] MAXIMUM_FRAGMENT_PAYLOAD_SIZES = {"1_KIB", "250_KIB", "500_KIB"};
    
    DTNBundle[] fragment(DTNBundle bundleToFragment);
    DTNBundle[] fragment(DTNBundle bundleToFragment, int fragmentPayloadSizeInBytes);
    DTNBundle defragment(DTNBundle[] fragmentsToCombine);
    boolean defragmentable(DTNBundle[] fragmentsToCombine);
}
