package net.minestom.server.network;

import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.server.ServerPacket;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SocketWriteTest {

    record IntPacket(int value) implements ServerPacket.Play {
        public static final NetworkBuffer.Type<IntPacket> SERIALIZER = NetworkBufferTemplate.template(
                INT, IntPacket::value,
                IntPacket::new);
    }

    record CompressiblePacket(String value) implements ServerPacket.Play {
        public static final NetworkBuffer.Type<CompressiblePacket> SERIALIZER = NetworkBufferTemplate.template(
                STRING, CompressiblePacket::value,
                CompressiblePacket::new);
    }

    @Test
    public void writeSingleUncompressed() {
        var packet = new IntPacket(5);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, -1);

        // 3 bytes length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals(3 + 1 + 4, buffer.writeIndex(), "Invalid buffer position");
    }

    @Test
    public void writeMultiUncompressed() {
        var packet = new IntPacket(5);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, -1);
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, -1);

        // 3 bytes length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals((3 + 1 + 4) * 2, buffer.writeIndex(), "Invalid buffer position");
    }

    @Test
    public void writeSingleCompressed() {
        var string = "Hello world!".repeat(200);
        var stringLength = string.getBytes(StandardCharsets.UTF_8).length;
        var lengthLength = getVarIntSize(stringLength);

        var packet = new CompressiblePacket(string);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, CompressiblePacket.SERIALIZER, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + payload
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertNotEquals(3 + 3 + 1 + lengthLength + stringLength, buffer.writeIndex(), "Buffer position does not account for compression");
    }

    @Test
    public void writeSingleCompressedSmall() {
        var packet = new IntPacket(5);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals(3 + 3 + 1 + 4, buffer.writeIndex(), "Invalid buffer position");
    }

    @Test
    public void writeMultiCompressedSmall() {
        var packet = new IntPacket(5);

        var buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, 256);
        PacketWriting.writeFramedPacket(buffer, IntPacket.SERIALIZER, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals((3 + 3 + 1 + 4) * 2, buffer.writeIndex(), "Invalid buffer position");
    }

    private static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }
}
