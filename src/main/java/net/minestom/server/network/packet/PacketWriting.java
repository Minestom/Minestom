package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Tools to write packets into a {@link NetworkBuffer} for network processing.
 * <p>
 * Fairly internal and performance sensitive.
 */
@ApiStatus.Internal
public final class PacketWriting {
    public static void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                         @NotNull ConnectionState state,
                                         @NotNull ClientPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                         @NotNull ConnectionState state,
                                         @NotNull ServerPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull PacketParser<T> parser,
                                             @NotNull ConnectionState state,
                                             @NotNull T packet,
                                             int compressionThreshold) {
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        writeFramedPacket(buffer, registry, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull PacketRegistry<T> registry,
                                             @NotNull T packet,
                                             int compressionThreshold) {
        final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo(packet);
        final int id = packetInfo.id();
        final NetworkBuffer.Type<T> serializer = packetInfo.serializer();
        writeFramedPacket(
                buffer, serializer,
                id, packet,
                compressionThreshold
        );
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull NetworkBuffer.Type<T> type,
                                             int id, @NotNull T packet,
                                             int compressionThreshold) {
        if (compressionThreshold <= 0) writeUncompressedFormat(buffer, type, id, packet);
        else writeCompressedFormat(buffer, type, id, packet, compressionThreshold);
    }

    private static <T> void writeUncompressedFormat(NetworkBuffer buffer,
                                                    NetworkBuffer.Type<T> type,
                                                    int id, T packet) {
        // Uncompressed format https://wiki.vg/Protocol#Without_compression
        final long lengthIndex = buffer.advanceWrite(3);
        buffer.write(NetworkBuffer.VAR_INT, id);
        buffer.write(type, packet);
        final long finalSize = buffer.writeIndex() - (lengthIndex + 3);
        buffer.writeAt(lengthIndex, NetworkBuffer.VAR_INT_3, (int) finalSize);
    }

    private static <T> void writeCompressedFormat(NetworkBuffer buffer,
                                                  NetworkBuffer.Type<T> type,
                                                  int id, T packet,
                                                  int compressionThreshold) {
        // Compressed format https://wiki.vg/Protocol#With_compression
        final long compressedIndex = buffer.advanceWrite(3);
        final long uncompressedIndex = buffer.advanceWrite(3);
        final long contentStart = buffer.writeIndex();
        buffer.write(NetworkBuffer.VAR_INT, id);
        buffer.write(type, packet);
        final long packetSize = buffer.writeIndex() - contentStart;
        final boolean compressed = packetSize >= compressionThreshold;
        if (compressed) {
            // Write the compressed content into the pooled buffer
            // and compress it into the current buffer
            NetworkBuffer input = PacketVanilla.PACKET_POOL.get();
            try {
                input.ensureWritable(packetSize);
                NetworkBuffer.copy(buffer, contentStart, input, 0, packetSize);
                buffer.writeIndex(contentStart);
                input.compress(0, packetSize, buffer);
            } finally {
                PacketVanilla.PACKET_POOL.add(input);
            }
        }
        // Packet header (Packet + Data Length)
        buffer.writeAt(compressedIndex, NetworkBuffer.VAR_INT_3, (int) (buffer.writeIndex() - uncompressedIndex));
        buffer.writeAt(uncompressedIndex, NetworkBuffer.VAR_INT_3, compressed ? (int) packetSize : 0);
    }

    public static NetworkBuffer allocateTrimmedPacket(@NotNull ConnectionState state,
                                                      @NotNull ClientPacket packet,
                                                      int compressionThreshold) {
        NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
        try {
            writeFramedPacket(buffer, state, packet, compressionThreshold);
            return buffer.copy(0, buffer.writeIndex());
        } finally {
            PacketVanilla.PACKET_POOL.add(buffer);
        }
    }

    public static NetworkBuffer allocateTrimmedPacket(@NotNull ConnectionState state,
                                                      @NotNull ServerPacket packet,
                                                      int compressionThreshold) {
        NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
        try {
            writeFramedPacket(buffer, state, packet, compressionThreshold);
            return buffer.copy(0, buffer.writeIndex());
        } finally {
            PacketVanilla.PACKET_POOL.add(buffer);
        }
    }
}
