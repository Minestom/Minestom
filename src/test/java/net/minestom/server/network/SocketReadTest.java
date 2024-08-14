package net.minestom.server.network;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.utils.PacketUtils;
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

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, ClientPluginMessagePacket.SERIALIZER, 0X0A, packet, compressed ? 256 : 0);

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer, compressed,
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

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, ClientPluginMessagePacket.SERIALIZER, 0x0A, packet, compressed ? 256 : 0);
        PacketUtils.writeFramedPacket(buffer, ClientPluginMessagePacket.SERIALIZER, 0x0A, packet, compressed ? 256 : 0);

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer, compressed,
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

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, ClientPluginMessagePacket.SERIALIZER, 0x0A, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.VAR_INT, 200); // incomplete 200 bytes packet

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(getVarIntSize(200), buffer.readableBytes());
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

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, ClientPluginMessagePacket.SERIALIZER, 0x0A, packet, compressed ? 256 : 0);
        buffer.write(NetworkBuffer.BYTE, (byte) -85); // incomplete var-int length

        List<Pair<Integer, NetworkBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer, compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertEquals(1, buffer.readableBytes());

        assertEquals(1, packets.size());
        var rawPacket = packets.getFirst();
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(rawPacket.right());
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    private static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }
}
