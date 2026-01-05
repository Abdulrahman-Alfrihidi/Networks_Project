package edu.kau.cpcs371.demux.io;

import edu.kau.cpcs371.demux.util.IpPortKey;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FlowFileWriter implements AutoCloseable {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // keep for debugging / future features
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private final IpPortKey key;
    private final Path path;
    private final OutputStream out;

    public FlowFileWriter(Path baseDir, IpPortKey key) throws IOException {
        this.key = key;
        // Spec: [timestamp]sourceip.sourceport-destip.destport
        String ts = LocalDateTime.now().format(TS);
        String safeFlow = sanitize(key.flowId());
        String fileName = "[" + ts + "]" + safeFlow; // no extension per spec
        this.path = baseDir.resolve(fileName);

        Files.createDirectories(baseDir);
        this.out = new BufferedOutputStream(Files.newOutputStream(this.path));
    }

    private static String sanitize(String s) {
        // Replace chars illegal/annoying in filenames (IPv6 ':', etc.)
        return s.replace(':', '_').replace('/', '_').replace('\\', '_');
    }

    public synchronized void write(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public Path path() { return path; }

    @Override public synchronized void close() throws IOException {
        out.flush();
        out.close();
    }
}
