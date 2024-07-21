package net.minestom.server.network;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static net.minestom.server.network.NetworkBuffer.INT;
import static net.minestom.server.network.NetworkBuffer.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SocketWriteTest {

    record IntPacket(int value) implements ServerPacket.Play {
        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(INT, value);
        }

    }

    record CompressiblePacket(String value) implements ServerPacket.Play {
        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, value);
        }

    }

    @Test
    public void writeSingleUncompressed() {
        var packet = new IntPacket(5);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 1, packet, -1);

        // 3 bytes length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals(3 + 1 + 4, buffer.position(), "Invalid buffer position");
    }

    @Test
    public void writeMultiUncompressed() {
        var packet = new IntPacket(5);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 1, packet, -1);
        PacketUtils.writeFramedPacket(buffer, 1, packet, -1);

        // 3 bytes length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals((3 + 1 + 4) * 2, buffer.position(), "Invalid buffer position");
    }

    @Test
    public void writeSingleCompressed() {
        var string = "Hello world!".repeat(200);
        var stringLength = string.getBytes(StandardCharsets.UTF_8).length;
        var lengthLength = Utils.getVarIntSize(stringLength);

        var packet = new CompressiblePacket(string);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + payload
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertNotEquals(3 + 3 + 1 + lengthLength + stringLength, buffer.position(), "Buffer position does not account for compression");
    }

    @Test
    public void writeSingleCompressedSmall() {
        var packet = new IntPacket(5);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals(3 + 3 + 1 + 4, buffer.position(), "Invalid buffer position");
    }

    @Test
    public void writeMultiCompressedSmall() {
        var packet = new IntPacket(5);

        var buffer = ObjectPool.PACKET_POOL.get();
        PacketUtils.writeFramedPacket(buffer, 1, packet, 256);
        PacketUtils.writeFramedPacket(buffer, 1, packet, 256);

        // 3 bytes packet length [var-int] + 3 bytes data length [var-int] + 1 byte packet id [var-int] + 4 bytes int
        // The 3 bytes var-int length is hardcoded for performance purpose, could change in the future
        assertEquals((3 + 3 + 1 + 4) * 2, buffer.position(), "Invalid buffer position");
    }
}
