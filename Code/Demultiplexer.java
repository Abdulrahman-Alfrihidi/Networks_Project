package edu.kau.cpcs371.demux.core;

import edu.kau.cpcs371.demux.io.FlowFileWriter;
import edu.kau.cpcs371.demux.util.IpPortKey;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Demultiplexer implements AutoCloseable {
    private final Path baseDir;
    private final Map<IpPortKey, FlowFileWriter> writers = new HashMap<>();

    public Demultiplexer(Path baseDir) {
        this.baseDir = baseDir;
    }

    public synchronized void route(IpPortKey key, byte[] data) throws IOException {
        FlowFileWriter w = writers.get(key);
        if (w == null) {
            w = new FlowFileWriter(baseDir, key);
            writers.put(key, w);
            System.out.println("Opened flow file: " + w.path().toAbsolutePath());
        }
        w.write(data);
    }

    @Override public synchronized void close() {
        for (FlowFileWriter w : writers.values()) {
            try { w.close(); } catch (Exception ignored) {}
        }
        writers.clear();
    }
}
