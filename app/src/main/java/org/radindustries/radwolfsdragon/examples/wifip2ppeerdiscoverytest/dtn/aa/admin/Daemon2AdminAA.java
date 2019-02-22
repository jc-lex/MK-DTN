package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.admin;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.CustodySignal;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.DTNBundle;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto.StatusReport;

public interface Daemon2AdminAA {
    void processAdminRecord(DTNBundle adminRecord);
    DTNBundle makeCustodySignal(
        DTNBundle userBundle, boolean custodyAccepted, CustodySignal.Reason reasonCode
    );
    DTNBundle makeStatusReport(
        DTNBundle userBundle, int statusCode, StatusReport.Reason reasonCode
    );
}
