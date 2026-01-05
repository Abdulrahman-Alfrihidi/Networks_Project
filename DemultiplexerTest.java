package edu.kau.cpcs371.demux.core;

import edu.kau.cpcs371.demux.util.IpPortKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DemultiplexerTest {

    @TempDir Path temp;

    @Test
    void routesDifferentFlowsToDifferentFiles() throws Exception {
        var demux = new Demultiplexer(temp);

        var k1 = new IpPortKey("10.0.0.1", 1111, "93.184.216.34", 80);
        var k2 = new IpPortKey("10.0.0.1", 1112, "93.184.216.34", 80);

        demux.route(k1, new byte[]{1,2,3});
        demux.route(k2, new byte[]{9,8,7});
        demux.close();

        List<Path> files = Files.list(temp).toList();
        assertEquals(2, files.size(), "two different flows → two files");

        // both non-empty
        assertTrue(Files.size(files.get(0)) > 0);
        assertTrue(Files.size(files.get(1)) > 0);
    }

    @Test
    void appendsToSameFlowFile() throws Exception {
        var demux = new Demultiplexer(temp);
        var key = new IpPortKey("10.0.0.2", 2222, "93.184.216.34", 443);

        demux.route(key, new byte[]{1,2});
        demux.route(key, new byte[]{3,4,5});
        demux.close();

        var files = Files.list(temp).toList();
        assertEquals(1, files.size(), "same flow → one file");
        assertEquals(5, Files.size(files.get(0)));
    }
}
