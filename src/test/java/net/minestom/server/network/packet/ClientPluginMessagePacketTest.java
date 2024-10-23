package net.minestom.server.network.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
