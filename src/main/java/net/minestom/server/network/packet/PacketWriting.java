package net.minestom.server.network.packet;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.function.BiPredicate;

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
                                         int compressionThreshold) throws IndexOutOfBoundsException {
        writeFramedPacket(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                         @NotNull ConnectionState state,
                                         @NotNull ServerPacket packet,
                                         int compressionThreshold) throws IndexOutOfBoundsException {
        writeFramedPacket(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull PacketParser<T> parser,
                                             @NotNull ConnectionState state,
                                             @NotNull T packet,
                                             int compressionThreshold) throws IndexOutOfBoundsException {
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        writeFramedPacket(buffer, registry, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull PacketRegistry<T> registry,
                                             @NotNull T packet,
                                             int compressionThreshold) throws IndexOutOfBoundsException {
        final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo(packet);
        writeFramedPacket(
                buffer,
                packetInfo, packet,
                compressionThreshold
        );
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer,
                                             @NotNull PacketRegistry.PacketInfo<T> packetInfo,
                                             @NotNull T packet,
                                             int compressionThreshold) throws IndexOutOfBoundsException {
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
                                             int compressionThreshold) throws IndexOutOfBoundsException {
        if (compressionThreshold <= 0) writeUncompressedFormat(buffer, type, id, packet);
        else writeCompressedFormat(buffer, type, id, packet, compressionThreshold);
    }

    private static <T> void writeUncompressedFormat(NetworkBuffer buffer,
                                                    NetworkBuffer.Type<T> type,
                                                    int id, T packet) throws IndexOutOfBoundsException {
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
                                                  int compressionThreshold) throws IndexOutOfBoundsException {
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
                if (input.capacity() < packetSize) input.resize(packetSize);
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
        return allocateTrimmedPacket(PacketVanilla.CLIENT_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static NetworkBuffer allocateTrimmedPacket(@NotNull ConnectionState state,
                                                      @NotNull ServerPacket packet,
                                                      int compressionThreshold) {
        return allocateTrimmedPacket(PacketVanilla.SERVER_PACKET_PARSER, state, packet, compressionThreshold);
    }

    public static <T> NetworkBuffer allocateTrimmedPacket(
            @NotNull PacketParser<T> parser,
            @NotNull ConnectionState state,
            @NotNull T packet,
            int compressionThreshold) {
        NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
        try {
            return allocateTrimmedPacket(buffer, parser, state, packet, compressionThreshold);
        } finally {
            PacketVanilla.PACKET_POOL.add(buffer);
        }
    }

    public static <T> NetworkBuffer allocateTrimmedPacket(
            @NotNull NetworkBuffer tmpBuffer,
            @NotNull PacketParser<T> parser,
            @NotNull ConnectionState state,
            @NotNull T packet,
            int compressionThreshold) {
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        return allocateTrimmedPacket(tmpBuffer, registry, packet, compressionThreshold);
    }

    public static <T> NetworkBuffer allocateTrimmedPacket(
            @NotNull NetworkBuffer tmpBuffer,
            @NotNull PacketRegistry<T> registry,
            @NotNull T packet,
            int compressionThreshold) {
        final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo(packet);
        final int id = packetInfo.id();
        final NetworkBuffer.Type<T> serializer = packetInfo.serializer();
        try {
            writeFramedPacket(tmpBuffer, serializer, id, packet, compressionThreshold);
            return tmpBuffer.copy(0, tmpBuffer.writeIndex());
        } catch (IndexOutOfBoundsException e) {
            final long sizeOf = serializer.sizeOf(packet, tmpBuffer.registries());
            if (sizeOf > ServerFlag.MAX_PACKET_SIZE) {
                throw new IllegalStateException("Packet too large: " + sizeOf);
            }
            // Add 15 bytes to account for the 3 potential varints in the packet header
            // Packet Length - Data Length - Packet ID
            tmpBuffer.resize(sizeOf + 15);
            tmpBuffer.writeIndex(0);
            writeFramedPacket(tmpBuffer, serializer, id, packet, compressionThreshold);
            return tmpBuffer.copy(0, tmpBuffer.writeIndex());
        }
    }

    public static <T> void writeQueue(NetworkBuffer buffer, Queue<T> queue, int minWrite,
                                      BiPredicate<NetworkBuffer, T> writer) {
        // The goal of this method is to write at the very least `minWrite` packets if the queue permits it.
        // The buffer is resized if it cannot hold this minimum.
        final int size = queue.size();
        minWrite = Math.min(minWrite, size);
        T packet;
        int written = 0;
        while ((packet = queue.peek()) != null) {
            final long index = buffer.writeIndex();
            boolean success;
            try {
                success = writer.test(buffer, packet);
            } catch (IndexOutOfBoundsException e) {
                success = false;
            }
            assert !success || buffer.writeIndex() > 0;
            // Poll the packet only if fully written
            if (success) {
                // Packet fully written
                queue.poll();
                written++;
            } else {
                buffer.writeIndex(index);
                if (written < minWrite) {
                    // Try again with a bigger buffer
                    final long newSize = Math.min(buffer.capacity() * 2, ServerFlag.MAX_PACKET_SIZE);
                    buffer.resize(newSize);
                } else {
                    // At least one packet has been written
                    // Not worth resizing to fit more, we'll try again next flush
                    break;
                }
            }
        }
    }
}
