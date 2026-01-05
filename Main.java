package edu.kau.cpcs371.demux;

import edu.kau.cpcs371.demux.capture.PacketCapturer;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== TCP Packet Demultiplexer ===");
        try {
            List<PcapNetworkInterface> ifs = Pcaps.findAllDevs();
            if (ifs == null || ifs.isEmpty()) {
                System.err.println("No capture devices found.");
                System.exit(2);
            }
            System.out.println("Found " + ifs.size() + " network interface(s):");
            for (int i = 0; i < ifs.size(); i++) {
                PcapNetworkInterface nif = ifs.get(i);
                System.out.printf(" [%d] %s  (name=%s, description=%s)%n",
                        i,
                        (nif.getName() != null ? nif.getName() : "<noname>"),
                        nif.getName(),
                        nif.getDescription());
            }

            int ifaceIndex = pickDefaultIndex(ifs);
            int maxPackets = 200; // default
            Path outDir = Path.of("./captures");

            for (String a : args) {
                if (a.startsWith("--ifaceIndex=")) {
                    try { ifaceIndex = Integer.parseInt(a.substring("--ifaceIndex=".length())); } catch (NumberFormatException ignore) {}
                } else if (a.startsWith("--max=")) {
                    try { maxPackets = Integer.parseInt(a.substring("--max=".length())); } catch (NumberFormatException ignore) {}
                } else if (a.startsWith("--out=")) {
                    outDir = Path.of(a.substring("--out=".length()));
                }
            }

            System.out.println("Using interface index: " + ifaceIndex);
            System.out.println("Max packets: " + maxPackets);
            System.out.println("Output dir: " + outDir.toAbsolutePath());

            new PacketCapturer().captureTcpToFiles(ifaceIndex, maxPackets, outDir);

        } catch (PcapNativeException e) {
            System.err.println("Pcap error: " + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int pickDefaultIndex(List<PcapNetworkInterface> ifs) {
        for (int i = 0; i < ifs.size(); i++) {
            var n = ifs.get(i);
            String desc = n.getDescription() == null ? "" : n.getDescription().toLowerCase();
            if (!desc.contains("loopback")) return i;
        }
        return 0;
    }
}
