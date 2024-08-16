package net.minestom.server.network.packet;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;

/**
 * Tools to write packets into a {@link NetworkBuffer} for network processing.
 * <p>
 * Fairly internal and performance sensitive.
 */
public final class PacketWriting {
    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull NetworkBuffer buffer,
                                         @NotNull ClientPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(PacketVanilla.CLIENT_PACKET_PARSER, state, buffer, packet, compressionThreshold);
    }

    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull NetworkBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(PacketVanilla.SERVER_PACKET_PARSER, state, buffer, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull PacketParser<T> parser,
                                             @NotNull ConnectionState state,
                                             @NotNull NetworkBuffer buffer,
                                             @NotNull T packet,
                                             int compressionThreshold) {
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        @SuppressWarnings("unchecked") final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo((Class<? extends T>) packet.getClass());
        final int id = packetInfo.id();
        final NetworkBuffer.Type<T> serializer = packetInfo.serializer();
        writeFramedPacket(
                buffer, serializer,
                id, packet,
                compressionThreshold
        );
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer, @NotNull NetworkBuffer.Type<T> type,
                                             int id, @NotNull T packet,
                                             int compressionThreshold) {
        if (compressionThreshold <= 0) writeUncompressedFormat(buffer, type, id, packet);
        else writeCompressedFormat(buffer, type, id, packet, compressionThreshold);
    }

    private static <T> void writeUncompressedFormat(NetworkBuffer buffer, NetworkBuffer.Type<T> type,
                                                    int id, T packet) {
        // Uncompressed format https://wiki.vg/Protocol#Without_compression
        final int lengthIndex = buffer.advanceWrite(3);
        buffer.write(NetworkBuffer.VAR_INT, id);
        buffer.write(type, packet);
        final int finalSize = buffer.writeIndex() - (lengthIndex + 3);
        writeVarIntHeader(buffer, lengthIndex, finalSize);
    }

    private static <T> void writeCompressedFormat(NetworkBuffer buffer, NetworkBuffer.Type<T> type,
                                                  int id, T packet,
                                                  int compressionThreshold) {
        // Compressed format https://wiki.vg/Protocol#With_compression
        final int compressedIndex = buffer.advanceWrite(3);
        final int uncompressedIndex = buffer.advanceWrite(3);
        final int contentStart = buffer.writeIndex();
        buffer.write(NetworkBuffer.VAR_INT, id);
        buffer.write(type, packet);
        final int packetSize = buffer.writeIndex() - contentStart;
        final boolean compressed = packetSize >= compressionThreshold;
        if (compressed) {
            // Packet large enough, compress it
            try (var hold = PacketVanilla.PACKET_POOL.hold()) {
                final NetworkBuffer input = hold.get();
                NetworkBuffer.copy(buffer, contentStart, input, 0, packetSize);
                buffer.writeIndex(contentStart);
                input.compress(0, packetSize, buffer);
            }
        }
        // Packet header (Packet + Data Length)
        writeVarIntHeader(buffer, compressedIndex, buffer.writeIndex() - uncompressedIndex);
        writeVarIntHeader(buffer, uncompressedIndex, compressed ? packetSize : 0);
    }

    private static void writeVarIntHeader(@NotNull NetworkBuffer buffer, int startIndex, int value) {
        buffer.writeAt(startIndex, BYTE, (byte) (value & 0x7F | 0x80));
        buffer.writeAt(startIndex + 1, BYTE, (byte) ((value >>> 7) & 0x7F | 0x80));
        buffer.writeAt(startIndex + 2, BYTE, (byte) (value >>> 14));
    }

    public static FramedPacket allocateTrimmedPacket(@NotNull ConnectionState state, @NotNull ServerPacket packet) {
        try (var hold = PacketVanilla.PACKET_POOL.hold()) {
            NetworkBuffer buffer = hold.get();
            writeFramedPacket(state, buffer, packet, MinecraftServer.getCompressionThreshold());
            final NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            return new FramedPacket(packet, copy);
        }
    }
}
