package edu.kau.cpcs371.demux.io;

import edu.kau.cpcs371.demux.util.IpPortKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlowFileWriterTest {

    @TempDir Path temp;

    @Test
    void createsFileWithExpectedNameAndWritesBytes() throws Exception {
        var key = new IpPortKey("192.168.1.5", 50000, "93.184.216.34", 80);
        byte[] data = {1,2,3,4,5};

        Path createdPath;
        try (var w = new FlowFileWriter(temp, key)) {
            createdPath = w.path();
            w.write(data);
        }

        assertTrue(Files.exists(createdPath), "file should exist");
        assertTrue(Files.size(createdPath) >= data.length, "file should contain written bytes");

        // name pattern: [timestamp]src.srcPort-dst.dstPort  (timestamp varies)
        var name = createdPath.getFileName().toString();
        assertTrue(name.contains("192.168.1.5.50000-93.184.216.34.80"));
        assertTrue(name.startsWith("["), "should start with [timestamp]");
    }
}
