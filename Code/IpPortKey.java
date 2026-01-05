package edu.kau.cpcs371.demux.util;

import java.util.Objects;

/** Immutable 4-tuple key: srcIP:srcPort -> dstIP:dstPort */
@SuppressWarnings("ClassCanBeRecord")
public final class IpPortKey {
    public final String srcIp;
    public final int srcPort;
    public final String dstIp;
    public final int dstPort;

    public IpPortKey(String srcIp, int srcPort, String dstIp, int dstPort) {
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
    }

    /** A normalized “flow id” string (handy for filenames/logs). */
    public String flowId() {
        return srcIp + "." + srcPort + "-" + dstIp + "." + dstPort;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IpPortKey that)) return false;
        return srcPort == that.srcPort
                && dstPort == that.dstPort
                && Objects.equals(srcIp, that.srcIp)
                && Objects.equals(dstIp, that.dstIp);
    }

    @Override public int hashCode() {
        return Objects.hash(srcIp, srcPort, dstIp, dstPort);
    }

    @Override public String toString() { return flowId(); }
}
