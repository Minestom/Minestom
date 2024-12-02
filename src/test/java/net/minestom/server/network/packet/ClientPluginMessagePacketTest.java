package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientPluginMessagePacketTest {

    @Test
    void testClientPluginMessagePacket() {
        ByteBuffer buf = ByteBuffer.allocateDirect(1024); // The default from NetworkBuffer
        NetworkBuffer networkBuffer = new NetworkBuffer(buf);
        ClientPluginMessagePacket clientPluginMessagePacket = new ClientPluginMessagePacket("channel", new byte[0]);

        clientPluginMessagePacket.write(networkBuffer);
        buf.limit(networkBuffer.writeIndex()); // Must set limit so that RAW_BYTES in plugin message packet has a len.

        var packet = new ClientPluginMessagePacket(networkBuffer);

        assertEquals("channel", packet.channel());
        assertArrayEquals(new byte[0], packet.data());
    }

}
