package net.minestom.server.network;

import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.client.play.ClientPluginMessagePacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.PooledBuffers;
import org.junit.jupiter.api.Test;

import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SocketReadTest {

    @Test
    public void completeUncompressed() throws DataFormatException {
        var packet = new ClientChatMessagePacket("Hello World!");

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 3, packet, 0);

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        var result = PacketUtils.readPackets(wrapper, false);
        assertNull(result.remaining());
        var packets = result.packets();
        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(3, rawPacket.id());
        var readPacket = new ClientChatMessagePacket(new BinaryReader(rawPacket.payload()));
        assertEquals("Hello World!", readPacket.message());
    }

    @Test
    public void completeCompressed() throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, 256);

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        var result = PacketUtils.readPackets(wrapper, true);
        assertNull(result.remaining());
        var packets = result.packets();
        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.id());
        var readPacket = new ClientPluginMessagePacket(new BinaryReader(rawPacket.payload()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }
}
