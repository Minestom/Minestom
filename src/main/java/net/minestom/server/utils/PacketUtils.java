package net.minestom.server.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuffer}
 * for network processing.
 * <p>
 * Note that all methods are mostly internal and can change at any moment.
 * This is due to their very unsafe nature (use of local buffers as cache) and their potential performance impact.
 * Be sure to check the implementation code.
 */
public final class PacketUtils {
    public static final ObjectPool<ByteBuffer> PACKET_POOL = new ObjectPool<>(() -> ByteBuffer.allocateDirect(ServerFlag.MAX_PACKET_SIZE), ByteBuffer::clear);
    private static final ThreadLocal<Deflater> LOCAL_DEFLATER = ThreadLocal.withInitial(Deflater::new);

    private static final PacketParser.Server SERVER_PACKET_PARSER = new PacketParser.Server();

    private PacketUtils() {
    }

    /**
     * Checks if the {@link ServerPacket} is suitable to be wrapped into a {@link CachedPacket}.
     * Note: {@link ServerPacket.ComponentHolding}s are not translated inside a {@link CachedPacket}.
     *
     * @see CachedPacket#body(ConnectionState)
     */
    static boolean shouldUseCachePacket(final @NotNull ServerPacket packet) {
        if (!MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION) return ServerFlag.GROUPED_PACKET;
        if (!(packet instanceof ServerPacket.ComponentHolding holder)) return ServerFlag.GROUPED_PACKET;
        return !containsTranslatableComponents(holder);
    }

    private static boolean containsTranslatableComponents(final @NotNull ComponentHolder<?> holder) {
        for (final Component component : holder.components()) {
            if (isTranslatable(component)) return true;
        }

        return false;
    }

    private static boolean isTranslatable(final @NotNull Component component) {
        if (component instanceof TranslatableComponent) return true;

        final var children = component.children();
        if (children.isEmpty()) return false;

        for (final Component child : children) {
            if (isTranslatable(child)) return true;
        }

        return false;
    }

    @ApiStatus.Internal
    public static @Nullable ByteBuffer readPackets(@NotNull ByteBuffer readBuffer, boolean compressed,
                                                   BiConsumer<Integer, ByteBuffer> payloadConsumer) throws DataFormatException {
        ByteBuffer remaining = null;
        ByteBuffer pool = PacketUtils.PACKET_POOL.get();
        while (readBuffer.hasRemaining()) {
            readBuffer.mark();
            try {
                // Ensure that the buffer contains the full packet (or wait for next socket read)
                final int packetLength = readVarInt(readBuffer);
                final int readerStart = readBuffer.position();
                if (readerStart + packetLength > readBuffer.limit()) {
                    // Integrity fail
                    throw new BufferUnderflowException();
                }
                // Read packet https://wiki.vg/Protocol#Packet_format
                ByteBuffer content = readBuffer;
                int decompressedSize = packetLength;
                if (compressed) {
                    final int dataLength = readVarInt(readBuffer);
                    final int payloadLength = packetLength - (readBuffer.position() - readerStart);
                    if (payloadLength < 0) {
                        throw new DataFormatException("Negative payload length " + payloadLength);
                    }
                    if (dataLength == 0) {
                        // Data is too small to be compressed, payload is following
                        decompressedSize = payloadLength;
                    } else {
                        // Decompress to content buffer
                        content = pool;
                        decompressedSize = dataLength;
                        Inflater inflater = new Inflater(); // TODO: Pool?
                        inflater.setInput(readBuffer.slice(readBuffer.position(), payloadLength));
                        inflater.inflate(content.slice(0, dataLength));
                        inflater.reset();
                    }
                }
                // Slice packet
                ByteBuffer payload = content.slice(content.position(), decompressedSize);
                final int packetId = readVarInt(payload);
                try {
                    payloadConsumer.accept(packetId, payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Position buffer to read the next packet
                readBuffer.position(readerStart + packetLength);
            } catch (BufferUnderflowException e) {
                readBuffer.reset();
                remaining = ByteBuffer.allocateDirect(readBuffer.remaining())
                        .put(readBuffer).flip();
                break;
            }
        }
        PacketUtils.PACKET_POOL.add(pool);
        return remaining;
    }

    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull ByteBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         boolean compression) {
        final PacketRegistry<ServerPacket> registry = SERVER_PACKET_PARSER.stateRegistry(state);
        final PacketRegistry.PacketInfo<ServerPacket> packetInfo = registry.packetInfo(packet.getClass());
        final int id = packetInfo.id();
        final NetworkBuffer.Type<ServerPacket> serializer = packetInfo.serializer();
        writeFramedPacket(buffer, id, serializer, packet, compression ? MinecraftServer.getCompressionThreshold() : 0);
    }

    public static <T> void writeFramedPacket(@NotNull ByteBuffer buffer,
                                             int id,
                                             @NotNull NetworkBuffer.Type<T> type,
                                             @NotNull T packet,
                                             int compressionThreshold) {
        NetworkBuffer networkBuffer = new NetworkBuffer(buffer, false);
        if (compressionThreshold <= 0) {
            // Uncompressed format https://wiki.vg/Protocol#Without_compression
            final int lengthIndex = networkBuffer.skipWrite(3);
            networkBuffer.write(NetworkBuffer.VAR_INT, id);
            type.write(networkBuffer, packet);
            final int finalSize = networkBuffer.writeIndex() - (lengthIndex + 3);
            writeVarIntHeader(buffer, lengthIndex, finalSize);
            buffer.position(networkBuffer.writeIndex());
            return;
        }
        // Compressed format https://wiki.vg/Protocol#With_compression
        final int compressedIndex = networkBuffer.skipWrite(3);
        final int uncompressedIndex = networkBuffer.skipWrite(3);

        final int contentStart = networkBuffer.writeIndex();
        networkBuffer.write(NetworkBuffer.VAR_INT, id);
        type.write(networkBuffer, packet);
        final int packetSize = networkBuffer.writeIndex() - contentStart;
        final boolean compressed = packetSize >= compressionThreshold;
        if (compressed) {
            // Packet large enough, compress it
            try (var hold = PacketUtils.PACKET_POOL.hold()) {
                final ByteBuffer input = hold.get().put(0, buffer, contentStart, packetSize);
                Deflater deflater = LOCAL_DEFLATER.get();
                deflater.setInput(input.limit(packetSize));
                deflater.finish();
                deflater.deflate(buffer.position(contentStart));
                deflater.reset();

                networkBuffer.skipWrite(buffer.position() - contentStart);
            }
        }
        // Packet header (Packet + Data Length)
        writeVarIntHeader(buffer, compressedIndex, networkBuffer.writeIndex() - uncompressedIndex);
        writeVarIntHeader(buffer, uncompressedIndex, compressed ? packetSize : 0);

        buffer.position(networkBuffer.writeIndex());
    }

    @ApiStatus.Internal
    public static ByteBuffer createFramedPacket(@NotNull ConnectionState state, @NotNull ByteBuffer buffer, @NotNull ServerPacket packet, boolean compression) {
        writeFramedPacket(state, buffer, packet, compression);
        return buffer.flip();
    }

    @ApiStatus.Internal
    public static ByteBuffer createFramedPacket(@NotNull ConnectionState state, @NotNull ByteBuffer buffer, @NotNull ServerPacket packet) {
        return createFramedPacket(state, buffer, packet, MinecraftServer.getCompressionThreshold() > 0);
    }

    @ApiStatus.Internal
    public static FramedPacket allocateTrimmedPacket(@NotNull ConnectionState state, @NotNull ServerPacket packet) {
        try (var hold = PacketUtils.PACKET_POOL.hold()) {
            final ByteBuffer temp = PacketUtils.createFramedPacket(state, hold.get(), packet);
            final int size = temp.remaining();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(size).put(0, temp, 0, size);
            return new FramedPacket(packet, buffer);
        }
    }

    @ApiStatus.Internal
    public static int invalidPacketState(@NotNull Class<?> packetClass, @NotNull ConnectionState state, @NotNull ConnectionState... expected) {
        assert expected.length > 0 : "Expected states cannot be empty: " + packetClass;
        StringBuilder expectedStr = new StringBuilder();
        for (ConnectionState connectionState : expected) {
            expectedStr.append(connectionState).append(", ");
        }
        expectedStr.delete(expectedStr.length() - 2, expectedStr.length());
        throw new IllegalStateException(String.format("Packet %s is not valid in state %s (only %s)", packetClass.getSimpleName(), state, expectedStr));
    }

    public static void writeVarIntHeader(@NotNull ByteBuffer buffer, int startIndex, int value) {
        buffer.put(startIndex, (byte) (value & 0x7F | 0x80));
        buffer.put(startIndex + 1, (byte) ((value >>> 7) & 0x7F | 0x80));
        buffer.put(startIndex + 2, (byte) (value >>> 14));
    }

    public static int readVarInt(ByteBuffer buf) {
        // https://github.com/jvm-profiling-tools/async-profiler/blob/a38a375dc62b31a8109f3af97366a307abb0fe6f/src/converter/one/jfr/JfrReader.java#L393
        int result = 0;
        for (int shift = 0; ; shift += 7) {
            byte b = buf.get();
            result |= (b & 0x7f) << shift;
            if (b >= 0) {
                return result;
            }
        }
    }
}
