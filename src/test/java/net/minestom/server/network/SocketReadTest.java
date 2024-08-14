package net.minestom.server.network;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SocketReadTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void complete(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);

        var wrapper = NetworkBuffer.wrap(buffer);
        wrapper.index(0, buffer.position());

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(0, remaining);

        assertEquals(1, packets.size());
        var rawPacket = packets.getFirst();
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(rawPacket.right());
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completeTwo(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);

        var wrapper = NetworkBuffer.wrap(buffer);
        wrapper.index(0, buffer.position());

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(0, remaining);

        assertEquals(2, packets.size());
        for (var rawPacket : packets) {
            assertEquals(0x0A, rawPacket.left());
            var readPacket = ClientPluginMessagePacket.SERIALIZER.read(rawPacket.right());
            assertEquals("channel", readPacket.channel());
            assertEquals(2000, readPacket.data().length);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void insufficientLength(boolean compressed) throws DataFormatException {
        // Write a complete packet then the next packet length without any payload

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        Utils.writeVarInt(buffer, 200); // incomplete 200 bytes packet

        var wrapper = NetworkBuffer.wrap(buffer);
        wrapper.index(0, buffer.position());

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(Utils.getVarIntSize(200), wrapper.readableBytes());
        assertEquals(200, remaining);

        assertEquals(1, packets.size());
        var rawPacket = packets.getFirst();
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(rawPacket.right());
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void incomplete(boolean compressed) throws DataFormatException {
        // Write a complete packet and incomplete var-int length for the next packet

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        buffer.put((byte) -85); // incomplete var-int length

        var wrapper = NetworkBuffer.wrap(buffer);
        wrapper.index(0, buffer.position());

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(wrapper, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(1, wrapper.readableBytes());

        assertEquals(1, packets.size());
        var rawPacket = packets.getFirst();
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(rawPacket.right());
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }
}
