package edu.kau.cpcs371.demux.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IpPortKeyTest {

    @Test
    void equalityAndHash() {
        var a = new IpPortKey("1.1.1.1", 1234, "2.2.2.2", 80);
        var b = new IpPortKey("1.1.1.1", 1234, "2.2.2.2", 80);
        var c = new IpPortKey("1.1.1.1", 1235, "2.2.2.2", 80);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void flowIdFormat() {
        var k = new IpPortKey("2001:db8::1", 5555, "2001:db8::2", 443);
        // flowId keeps ':' (sanitizing happens in file writer)
        assertEquals("2001:db8::1.5555-2001:db8::2.443", k.flowId());
    }
}
