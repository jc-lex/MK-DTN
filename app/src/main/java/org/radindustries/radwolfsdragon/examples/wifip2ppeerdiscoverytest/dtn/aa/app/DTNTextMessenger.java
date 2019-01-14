package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNTextMessage;

import java.util.List;

public interface DTNTextMessenger {
    List<DTNTextMessage> getDeliveredTextMessages();
}
