package net.minestom.server.network;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.network.packet.client.play.ClientPluginMessagePacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Utils;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.PooledBuffers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.*;

public class SocketReadTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void complete(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, compressed ? 256 : 0);

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNull(remaining);

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = new ClientPluginMessagePacket(new BinaryReader(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completeTwo(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, compressed ? 256 : 0);
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, compressed ? 256 : 0);

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNull(remaining);

        assertEquals(2, packets.size());
        for (var rawPacket : packets) {
            assertEquals(0x0A, rawPacket.left());
            var readPacket = new ClientPluginMessagePacket(new BinaryReader(rawPacket.right()));
            assertEquals("channel", readPacket.channel());
            assertEquals(2000, readPacket.data().length);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void insufficientLength(boolean compressed) throws DataFormatException {
        // Write a complete packet then the next packet length without any payload

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, compressed ? 256 : 0);
        Utils.writeVarInt(buffer, 200); // incomplete 200 bytes packet

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNotNull(remaining);
        assertEquals(Utils.getVarIntSize(200), remaining.readableBytes());

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = new ClientPluginMessagePacket(new BinaryReader(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void incomplete(boolean compressed) throws DataFormatException {
        // Write a complete packet and incomplete var-int length for the next packet

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PooledBuffers.packetBuffer();
        PacketUtils.writeFramedPacket(buffer, 0x0A, packet, compressed ? 256 : 0);
        buffer.put((byte) -85); // incomplete var-int length

        var wrapper = BinaryBuffer.wrap(buffer);
        wrapper.reset(0, buffer.position());

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNotNull(remaining);
        assertEquals(1, remaining.readableBytes());

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = new ClientPluginMessagePacket(new BinaryReader(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }
}
