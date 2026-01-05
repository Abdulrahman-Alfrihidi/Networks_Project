package edu.kau.cpcs371.demux.capture;

import edu.kau.cpcs371.demux.core.Demultiplexer;
import edu.kau.cpcs371.demux.util.IpPortKey;
import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.nio.file.Path;

import static org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;

public class PacketCapturer {

    /** Old demo */
    @SuppressWarnings("unused")
    public void captureTcp(int ifaceIndex, int maxPackets) throws PcapNativeException, NotOpenException {
        var ifs = Pcaps.findAllDevs();
        if (ifs == null || ifs.isEmpty()) throw new PcapNativeException("No capture devices found");
        if (ifaceIndex < 0 || ifaceIndex >= ifs.size()) throw new IllegalArgumentException("Bad iface index");

        PcapNetworkInterface nif = ifs.get(ifaceIndex);
        System.out.println("Opening: " + (nif.getDescription() != null ? nif.getDescription() : nif.getName()));

        int snapLen = 65536, timeoutMs = 10;
        try (PcapHandle handle = nif.openLive(snapLen, PromiscuousMode.PROMISCUOUS, timeoutMs)) {
            handle.setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
            System.out.println("Capturing TCP packets... (will stop after " + maxPackets + " packets)");
            try {
                handle.loop(maxPackets, this::logPacket);
            } catch (InterruptedException ie) {
                System.out.println("Capture interrupted: " + ie.getMessage());
                Thread.currentThread().interrupt();
            }
            System.out.println("Capture finished.");
        }
    }

    /** New method: demux TCP packets into per-flow files in outDir */
    public void captureTcpToFiles(int ifaceIndex, int maxPackets, Path outDir)
            throws PcapNativeException, NotOpenException {
        var ifs = Pcaps.findAllDevs();
        if (ifs == null || ifs.isEmpty()) throw new PcapNativeException("No capture devices found");
        if (ifaceIndex < 0 || ifaceIndex >= ifs.size()) throw new IllegalArgumentException("Bad iface index");

        PcapNetworkInterface nif = ifs.get(ifaceIndex);
        System.out.println("Opening: " + (nif.getDescription() != null ? nif.getDescription() : nif.getName()));

        int snapLen = 65536, timeoutMs = 10;
        try (PcapHandle handle = nif.openLive(snapLen, PromiscuousMode.PROMISCUOUS, timeoutMs);
             Demultiplexer demux = new Demultiplexer(outDir)) {

            handle.setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);
            System.out.println("Capturing TCP -> writing to: " + outDir.toAbsolutePath() +
                    " (maxPackets=" + maxPackets + ")");

            try {
                final int[] count = {0};
                handle.loop(maxPackets, (Packet packet) -> {
                    TcpPacket tcp = packet.get(TcpPacket.class);
                    if (tcp == null) return; // not TCP (shouldn't happen due to filter)
                    IpPortKey key = extractKey(packet, tcp);
                    if (key == null) return;  // no IPv4/IPv6 header
                    byte[] raw = packet.getRawData(); // store raw packet bytes (simple & robust)
                    try {
                        demux.route(key, raw);
                    } catch (Exception e) {
                        System.err.println("Write failed for " + key + ": " + e.getMessage());
                    }
                    if (++count[0] % 50 == 0) {
                        System.out.println("...captured " + count[0] + " packets");
                    }
                });
            } catch (InterruptedException ie) {
                System.out.println("Capture interrupted: " + ie.getMessage());
                Thread.currentThread().interrupt();
            }

            System.out.println("Capture finished. Files are in: " + outDir.toAbsolutePath());
        }
    }

    private void logPacket(Packet packet) {
        TcpPacket tcp = packet.get(TcpPacket.class);
        if (tcp == null) return;

        String src = "unknown", dst = "unknown";
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 != null) {
            src = ipv4.getHeader().getSrcAddr().getHostAddress();
            dst = ipv4.getHeader().getDstAddr().getHostAddress();
        } else {
            IpV6Packet ipv6 = packet.get(IpV6Packet.class);
            if (ipv6 != null) {
                src = ipv6.getHeader().getSrcAddr().getHostAddress();
                dst = ipv6.getHeader().getDstAddr().getHostAddress();
            }
        }

        int sport = tcp.getHeader().getSrcPort().valueAsInt();
        int dport = tcp.getHeader().getDstPort().valueAsInt();
        System.out.printf("%s:%d -> %s:%d len=%d%n", src, sport, dst, dport, packet.length());
    }

    private IpPortKey extractKey(Packet packet, TcpPacket tcp) {
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 != null) {
            var h = ipv4.getHeader();
            return new IpPortKey(
                    h.getSrcAddr().getHostAddress(),
                    tcp.getHeader().getSrcPort().valueAsInt(),
                    h.getDstAddr().getHostAddress(),
                    tcp.getHeader().getDstPort().valueAsInt());
        }
        IpV6Packet ipv6 = packet.get(IpV6Packet.class);
        if (ipv6 != null) {
            var h = ipv6.getHeader();
            return new IpPortKey(
                    h.getSrcAddr().getHostAddress(),
                    tcp.getHeader().getSrcPort().valueAsInt(),
                    h.getDstAddr().getHostAddress(),
                    tcp.getHeader().getDstPort().valueAsInt());
        }
        return null; // neither v4 nor v6
    }
}
