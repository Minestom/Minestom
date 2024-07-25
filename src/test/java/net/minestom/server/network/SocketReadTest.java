package net.minestom.server.network;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.network.packet.client.common.ClientPluginMessagePacket;
import net.minestom.server.utils.PacketUtils;
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

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer.flip(), compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNull(remaining);

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(new NetworkBuffer(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void completeTwo(boolean compressed) throws DataFormatException {
        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer.flip(), compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNull(remaining);

        assertEquals(2, packets.size());
        for (var rawPacket : packets) {
            assertEquals(0x0A, rawPacket.left());
            var readPacket = ClientPluginMessagePacket.SERIALIZER.read(new NetworkBuffer(rawPacket.right()));
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
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        writeVarInt(buffer, 200); // incomplete 200 bytes packet

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer.flip(), compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNotNull(remaining);
        assertEquals(getVarIntSize(200), remaining.remaining());

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(new NetworkBuffer(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void incomplete(boolean compressed) throws DataFormatException {
        // Write a complete packet and incomplete var-int length for the next packet

        var packet = new ClientPluginMessagePacket("channel", new byte[2000]);

        var buffer = PacketUtils.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 0x0A, ClientPluginMessagePacket.SERIALIZER, packet, compressed ? 256 : 0);
        buffer.put((byte) -85); // incomplete var-int length

        List<Pair<Integer, ByteBuffer>> packets = new ArrayList<>();
        var remaining = PacketUtils.readPackets(buffer.flip(), compressed,
                (integer, payload) -> packets.add(Pair.of(integer, payload)));
        assertNotNull(remaining);
        assertEquals(1, remaining.remaining());

        assertEquals(1, packets.size());
        var rawPacket = packets.get(0);
        assertEquals(0x0A, rawPacket.left());
        var readPacket = ClientPluginMessagePacket.SERIALIZER.read(new NetworkBuffer(rawPacket.right()));
        assertEquals("channel", readPacket.channel());
        assertEquals(2000, readPacket.data().length);
    }

    private static void writeVarInt(ByteBuffer buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.put((byte) value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            buf.putShort((short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            buf.put((byte) (value & 0x7F | 0x80));
            buf.put((byte) ((value >>> 7) & 0x7F | 0x80));
            buf.put((byte) (value >>> 14));
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            buf.putInt((value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
        } else {
            buf.putInt((value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
            buf.put((byte) (value >>> 28));
        }
    }

    private static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }
}
