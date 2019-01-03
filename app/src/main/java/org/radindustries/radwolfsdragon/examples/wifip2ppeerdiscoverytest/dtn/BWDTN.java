package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn;

import android.content.Context;
import android.support.annotation.NonNull;

import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.admin.Daemon2AdminAA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNClient;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.aa.app.DTNUI;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.cla.Daemon2CLA;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.fragmentmanager.Daemon2FragmentManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.DTNManager;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.manager.Daemon2Managable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.peerdiscoverer.Daemon2PeerDiscoverer;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2NECTARRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2PRoPHETRoutingTable;
import org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.router.Daemon2Router;

public final class BWDTN {
    private static RadAppAA radAppAA = null;
    private static RadAdminAA radAdminAA = null;
    private static RadFragMgr radFragMgr = null;
    private static RadDiscoverer radDiscoverer = null;
    private static RadCLA radCLA = null;
    private static RadManager radManager = null;
    private static RadRouter radRouter = null;
    private static RadNECTARRoutingTable radNECTARRoutingTable = null;
    private static RadPRoPHETRoutingTable radPRoPHETRoutingTable = null;
    private static RadDaemon radDaemon = null;

    private BWDTN() {}
    
    public static DTNClient getDTNClient() {
        if (radDaemon == null) return null;
        else return radAppAA;
    }
    
    public static DTNManager getDTNManager() {
        if (radDaemon == null) return null;
        else return radManager;
    }
    
    public static void init(@NonNull Context context, @NonNull DTNUI ui) {
        if (radDaemon == null) {
            radDaemon = new RadDaemon();
            
            radAppAA = new RadAppAA(ui, radDaemon);
            radManager = new RadManager(radDaemon);
    
            radDaemon.setCLA(getCLA(context));
            radDaemon.setDiscoverer(getPeerDiscoverer(context));
            radDaemon.setAppAA(radAppAA);
            radDaemon.setAdminAA(getAdminAA());
            radDaemon.setFragmentManager(getFragmentManager());
            radDaemon.setRouter(getRouter());
            radDaemon.setNECTARRoutingTable(getNECTARRoutingTable(context));
            radDaemon.setPRoPHETRoutingTable(getPRoPHETRoutingTable(context));
            radDaemon.setManagables(new Daemon2Managable[]{
                radDiscoverer, radCLA, radPRoPHETRoutingTable
            });
        }
    }
    
    private static Daemon2FragmentManager getFragmentManager() {
        if (radFragMgr == null) {
            radFragMgr = new RadFragMgr();
        }
        return radFragMgr;
    }
    
    private static Daemon2AdminAA getAdminAA() {
        if (radAppAA == null) radAdminAA = new RadAdminAA(radDaemon);
        return radAdminAA;
    }
    
    private static Daemon2PeerDiscoverer getPeerDiscoverer(@NonNull Context context) {
        if (radCLA == null) radCLA = new RadCLA(radDaemon, radDaemon, context);
        if (radDiscoverer == null)
            radDiscoverer = new RadDiscoverer(radDaemon, radDaemon, radDaemon, radCLA, context);
        return radDiscoverer;
    }
    
    private static Daemon2CLA getCLA(@NonNull Context context) {
        if (radCLA == null) radCLA = new RadCLA(radDaemon, radDaemon, context);
        return radCLA;
    }
    
    private static Daemon2Router getRouter() {
        if (radRouter == null) radRouter = new RadRouter(radDaemon, radDaemon, radDaemon);
        return radRouter;
    }
    
    private static Daemon2NECTARRoutingTable getNECTARRoutingTable(@NonNull Context context) {
        if (radNECTARRoutingTable == null)
            radNECTARRoutingTable = new RadNECTARRoutingTable(context);
        return radNECTARRoutingTable;
    }
    
    private static Daemon2PRoPHETRoutingTable getPRoPHETRoutingTable(@NonNull Context context) {
        if (radPRoPHETRoutingTable == null)
            radPRoPHETRoutingTable = new RadPRoPHETRoutingTable(context);
        return radPRoPHETRoutingTable;
    }
}
