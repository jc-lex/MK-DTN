package org.radindustries.radwolfsdragon.examples.wifip2ppeerdiscoverytest.dtn.dto;

import java.util.Objects;

public final class DTNNode {
    public String eid;
    public String CLAAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DTNNode)) return false;
        DTNNode dtnNode = (DTNNode) o;
        return Objects.equals(eid, dtnNode.eid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eid);
    }

    @Override
    public String toString() {
        return "DTNNode {" +
                "\neid=" + eid +
                ",\nCLAAddress=" + CLAAddress +
                "\n}";
    }
}
