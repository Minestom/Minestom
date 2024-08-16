package net.minestom.server.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBuffer.Type;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.zip.DataFormatException;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuffer}
 * for network processing.
 * <p>
 * Note that all methods are mostly internal and can change at any moment.
 * This is due to their very unsafe nature (use of local buffers as cache) and their potential performance impact.
 * Be sure to check the implementation code.
 */
public final class PacketUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(PacketUtils.class);

    private static final PacketParser<ClientPacket> CLIENT_PACKET_PARSER = new PacketParser.Client();
    private static final PacketParser<ServerPacket> SERVER_PACKET_PARSER = new PacketParser.Server();

    public static final ObjectPool<NetworkBuffer> PACKET_POOL = ObjectPool.pool(
            () -> NetworkBuffer.staticBuffer(ServerFlag.MAX_PACKET_SIZE, MinecraftServer.process()),
            NetworkBuffer::clear);

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

    public static ConnectionState nextClientState(ClientPacket packet, ConnectionState currentState) {
        return switch (packet) {
            case ClientHandshakePacket handshakePacket -> switch (handshakePacket.intent()) {
                case STATUS -> ConnectionState.STATUS;
                case LOGIN, TRANSFER -> ConnectionState.LOGIN;
            };
            case ClientLoginAcknowledgedPacket ignored -> ConnectionState.CONFIGURATION;
            case ClientFinishConfigurationPacket ignored -> ConnectionState.PLAY;
            default -> currentState;
        };
    }

    public record ReadResult<T>(List<T> packets, ConnectionState newState, int missingLength) {
    }

    public static ReadResult<ClientPacket> readClients(
            @NotNull ConnectionState state,
            @NotNull NetworkBuffer readBuffer, boolean compressed
    ) throws DataFormatException {
        return readPackets(CLIENT_PACKET_PARSER, state, PacketUtils::nextClientState, readBuffer, compressed);
    }

    public static <T> ReadResult<T> readPackets(
            @NotNull PacketParser<T> parser,
            @NotNull ConnectionState state, BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            @NotNull NetworkBuffer readBuffer, boolean compressed
    ) throws DataFormatException {
        List<T> packets = new ArrayList<>();
        while (readBuffer.readableBytes() > 0) {
            final int beginMark = readBuffer.readIndex();
            try {
                // Ensure that the buffer contains the full packet (or wait for next socket read)
                final int packetLength = readBuffer.read(VAR_INT);
                if (readBuffer.readIndex() > readBuffer.writeIndex()) {
                    // Can't read the packet length
                    readBuffer.readIndex(beginMark);
                    return new ReadResult<>(packets, state, 0);
                }
                final int readerStart = readBuffer.readIndex();
                if (readBuffer.readableBytes() < packetLength) {
                    // Can't read the full packet
                    final int missingLength = packetLength - readBuffer.readableBytes();
                    readBuffer.readIndex(beginMark);
                    return new ReadResult<>(packets, state, missingLength);
                }
                // Read packet https://wiki.vg/Protocol#Packet_format
                NetworkBuffer content = readBuffer.slice(readBuffer.readIndex(), packetLength);
                T packet;
                if (compressed) {
                    final int dataLength = content.read(VAR_INT);
                    if (dataLength > 0) {
                        NetworkBuffer decompressed = PACKET_POOL.get();
                        try {
                            content.decompress(content.readIndex(), content.readableBytes(), decompressed);
                            packet = readUncompressedPacket(parser, state, decompressed);
                        } finally {
                            PACKET_POOL.add(decompressed);
                        }
                    } else {
                        packet = readUncompressedPacket(parser, state, content);
                    }
                } else {
                    packet = readUncompressedPacket(parser, state, content);
                }
                packets.add(packet);
                state = stateUpdater.apply(packet, state);
                // Position buffer to read the next packet
                readBuffer.readIndex(readerStart + packetLength);
            } catch (BufferUnderflowException e) {
                readBuffer.readIndex(beginMark);
                return new ReadResult<>(packets, state, 0);
            }
        }
        return new ReadResult<>(packets, state, 0);
    }

    private static <T> T readUncompressedPacket(PacketParser<T> parser,
                                                @NotNull ConnectionState state,
                                                @NotNull NetworkBuffer buffer) {
        final int packetId = buffer.read(VAR_INT);
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo(packetId);
        final Type<T> serializer = packetInfo.serializer();
        try {
            final T packet = serializer.read(buffer);
            if (buffer.readableBytes() != 0) {
                var info = parser.stateRegistry(state).packetInfo(packetId);
                LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({})", info.packetClass().getSimpleName(), Integer.toHexString(packetId), buffer);
            }
            return packet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull NetworkBuffer buffer,
                                         @NotNull ClientPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(CLIENT_PACKET_PARSER, state, buffer, packet, compressionThreshold);
    }

    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull NetworkBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         int compressionThreshold) {
        writeFramedPacket(SERVER_PACKET_PARSER, state, buffer, packet, compressionThreshold);
    }

    public static <T> void writeFramedPacket(@NotNull PacketParser<T> parser,
                                             @NotNull ConnectionState state,
                                             @NotNull NetworkBuffer buffer,
                                             @NotNull T packet,
                                             int compressionThreshold) {
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        @SuppressWarnings("unchecked") final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo((Class<? extends T>) packet.getClass());
        final int id = packetInfo.id();
        final Type<T> serializer = packetInfo.serializer();
        writeFramedPacket(
                buffer, serializer,
                id, packet,
                compressionThreshold
        );
    }

    public static <T> void writeFramedPacket(@NotNull NetworkBuffer buffer, @NotNull Type<T> type,
                                             int id, @NotNull T packet,
                                             int compressionThreshold) {
        if (compressionThreshold <= 0) writeUncompressedFormat(buffer, type, id, packet);
        else writeCompressedFormat(buffer, type, id, packet, compressionThreshold);
    }

    private static <T> void writeUncompressedFormat(NetworkBuffer buffer, Type<T> type,
                                                    int id, T packet) {
        // Uncompressed format https://wiki.vg/Protocol#Without_compression
        final int lengthIndex = buffer.advanceWrite(3);
        buffer.write(NetworkBuffer.VAR_INT, id);
        buffer.write(type, packet);
        final int finalSize = buffer.writeIndex() - (lengthIndex + 3);
        writeVarIntHeader(buffer, lengthIndex, finalSize);
    }

    private static <T> void writeCompressedFormat(NetworkBuffer buffer, Type<T> type,
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
            try (var hold = PACKET_POOL.hold()) {
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

    @ApiStatus.Internal
    public static FramedPacket allocateTrimmedPacket(@NotNull ConnectionState state, @NotNull ServerPacket packet) {
        try (var hold = PACKET_POOL.hold()) {
            NetworkBuffer buffer = hold.get();
            writeFramedPacket(state, buffer, packet, MinecraftServer.getCompressionThreshold());
            final NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            return new FramedPacket(packet, copy);
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
}
