package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClientPluginMessagePacketTest {

    @Test
    void testClientPluginMessagePacket() {
        var array = NetworkBuffer.makeArray(
                ClientPluginMessagePacket.SERIALIZER,
                new ClientPluginMessagePacket("channel", new byte[0]));

        var readBuffer = NetworkBuffer.wrap(array, 0, array.length);
        var packet = readBuffer.read(ClientPluginMessagePacket.SERIALIZER);

        assertEquals("channel", packet.channel());
        assertArrayEquals(new byte[0], packet.data());
    }

    @Test
    void testClientPluginMessagePacketClone() {
        var bytes = new byte[]{0x10, 0x11};
        var message = new ClientPluginMessagePacket("channel", bytes);
        message.data()[0] = 0x00;
        assertArrayEquals(new byte[]{0x00, 0x11}, message.data());
        assertNotSame(message.data(), bytes);
    }

}
