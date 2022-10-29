package net.minestom.server.network.socket;

import net.minestom.server.network.PacketProcessor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ServerAddressTest {

    @Test
    public void inetAddressTest() throws IOException {
        InetSocketAddress address = new InetSocketAddress("localhost", 0);
        var server = new Server(new PacketProcessor());
        server.init(address);
        assertSame(address, server.socketAddress());
        assertEquals(address.getHostString(), server.getAddress());
        assertEquals(address.getPort(), server.getPort());

        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);
    }

    @Test
    public void unixAddressTest() throws IOException {
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of("minestom.sock");
        var server = new Server(new PacketProcessor());
        server.init(address);
        assertTrue(Files.exists(address.getPath()));
        assertSame(address, server.socketAddress());
        assertEquals("unix://" + address.getPath(), server.getAddress());
        assertEquals(0, server.getPort());

        assertDoesNotThrow(server::start);
        assertDoesNotThrow(server::stop);
        assertFalse(Files.exists(address.getPath()), "The socket file should be deleted");
    }

    @Test
    public void noAddressTest() throws IOException {
        var server = new Server(new PacketProcessor());
        assertDoesNotThrow(server::stop);
    }
}
