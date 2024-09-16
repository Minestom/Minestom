package net.minestom.server.network.packet;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.zip.DataFormatException;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Tools to read packets from a {@link NetworkBuffer} for network processing.
 * <p>
 * Fairly internal and performance sensitive.
 */
@SuppressWarnings("ALL")
@ApiStatus.Internal
public final class PacketReading {
    private final static Logger LOGGER = LoggerFactory.getLogger(PacketReading.class);

    private static final int MAX_VAR_INT_SIZE = 5;
    private static final Result.Empty EMPTY_CLIENT_PACKET = new Result.Empty<>();

    public sealed interface Result<T> {

        /**
         * At least one packet was read.
         * The buffer may still contain half-read packets and should therefore be compacted for next read.
         */
        record Success<T>(List<T> packets, ConnectionState newState) implements Result<T> {
            public Success {
                if (packets.isEmpty()) {
                    throw new IllegalArgumentException("Empty packets");
                }
            }

            public Success(T packet, ConnectionState newState) {
                this(List.of(packet), newState);
            }
        }

        /**
         * Represents no packet to read. Can generally be ignored.
         * <p>
         * Happens when a packet length or payload couldn't be read, but the buffer has enough capacity.
         */
        record Empty<T>() implements Result<T> {
        }

        /**
         * Represents a failure to read a packet due to insufficient buffer capacity.
         * <p>
         * Buffer should be expanded to at least {@code requiredCapacity} bytes.
         * <p>
         * If the buffer does not allow to read the packet length, max var-int length is returned.
         */
        record Failure<T>(long requiredCapacity) implements Result<T> {
        }
    }

    public static Result<ClientPacket> readClients(
            @NotNull NetworkBuffer buffer,
            @NotNull ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, PacketVanilla::nextClientState, compressed);
    }

    public static Result<ServerPacket> readServers(
            @NotNull NetworkBuffer buffer,
            @NotNull ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPackets(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, PacketVanilla::nextServerState, compressed);
    }

    public static <T> Result<T> readPackets(
            @NotNull NetworkBuffer buffer,
            @NotNull PacketParser<T> parser,
            @NotNull ConnectionState state,
            @NotNull BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed
    ) throws DataFormatException {
        List<T> packets = new ArrayList<>();
        readLoop:
        while (buffer.readableBytes() > 0) {
            final Result<T> result = readPacket(buffer, parser, state, stateUpdater, compressed);
            if (buffer.readableBytes() == 0 && packets.isEmpty()) return result;
            switch (result) {
                case Result.Success<T> success -> {
                    assert success.packets().size() == 1;
                    packets.add(success.packets().getFirst());
                    state = success.newState();
                }
                case Result.Empty<T> ignored -> {
                    break readLoop;
                }
                case Result.Failure<T> failure -> {
                    return packets.isEmpty() ? failure : new Result.Success<>(packets, state);
                }
            }
        }
        return !packets.isEmpty() ? new Result.Success<>(packets, state) : EMPTY_CLIENT_PACKET;
    }

    public static Result<ClientPacket> readClient(
            @NotNull NetworkBuffer buffer,
            @NotNull ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPacket(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, PacketVanilla::nextClientState, compressed);
    }

    public static Result<ServerPacket> readServer(
            @NotNull NetworkBuffer buffer,
            @NotNull ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPacket(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, PacketVanilla::nextServerState, compressed);
    }

    public static <T> Result<T> readPacket(
            @NotNull NetworkBuffer buffer,
            @NotNull PacketParser<T> parser,
            @NotNull ConnectionState state,
            @NotNull BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed
    ) throws DataFormatException {
        final long beginMark = buffer.readIndex();
        // READ PACKET LENGTH
        final int packetLength;
        try {
            packetLength = buffer.read(VAR_INT);
        } catch (IndexOutOfBoundsException e) {
            // Couldn't read a single var-int
            return new Result.Failure<>(MAX_VAR_INT_SIZE);
        }
        final long readerStart = buffer.readIndex();
        if (readerStart > buffer.writeIndex()) {
            // Can't read the packet length, buffer has enough capacity
            buffer.readIndex(beginMark);
            return EMPTY_CLIENT_PACKET;
        }
        final int maxPacketSize = maxPacketSize(state);
        if (packetLength > maxPacketSize) {
            throw new DataFormatException("Packet too large: " + packetLength);
        }
        // READ PAYLOAD https://wiki.vg/Protocol#Packet_format
        if (buffer.readableBytes() < packetLength) {
            // Can't read the full packet
            buffer.readIndex(beginMark);
            final long packetLengthVarIntSize = readerStart - beginMark;
            final long requiredCapacity = packetLengthVarIntSize + packetLength;
            // Must return a failure if the buffer is too small
            // Otherwise do nothing, and hope to read the packet remains next time
            if (requiredCapacity > buffer.capacity()) return new Result.Failure<>(requiredCapacity);
            else return EMPTY_CLIENT_PACKET;
        }
        final long readerEnd = readerStart + packetLength;
        final long writerEnd = buffer.writeIndex();
        buffer.writeIndex(readerEnd);
        final PacketRegistry<T> registry = parser.stateRegistry(state);
        final T packet = readFramedPacket(buffer, registry, compressed);
        final ConnectionState nextState = stateUpdater.apply(packet, state);
        buffer.index(readerEnd, writerEnd);
        return new Result.Success<>(packet, nextState);
    }

    private static <T> T readFramedPacket(NetworkBuffer buffer,
                                          PacketRegistry<T> registry,
                                          boolean compressed) throws DataFormatException {
        if (!compressed) {
            // No compression format
            return readPayload(buffer, registry);
        }

        final int dataLength = buffer.read(VAR_INT);
        if (dataLength == 0) {
            // Uncompressed packet
            return readPayload(buffer, registry);
        }

        // Decompress the packet into the pooled buffer
        // and read the uncompressed packet from it
        NetworkBuffer decompressed = PacketVanilla.PACKET_POOL.get();
        try {
            if (decompressed.capacity() < dataLength) decompressed.resize(dataLength);
            buffer.decompress(buffer.readIndex(), buffer.readableBytes(), decompressed);
            return readPayload(decompressed, registry);
        } finally {
            PacketVanilla.PACKET_POOL.add(decompressed);
        }
    }

    private static <T> T readPayload(NetworkBuffer buffer, PacketRegistry<T> registry) {
        final int packetId = buffer.read(VAR_INT);
        final PacketRegistry.PacketInfo<T> packetInfo = registry.packetInfo(packetId);
        final NetworkBuffer.Type<T> serializer = packetInfo.serializer();
        try {
            final T packet = serializer.read(buffer);
            if (buffer.readableBytes() != 0) {
                LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({})",
                        packetInfo.packetClass().getSimpleName(), Integer.toHexString(packetId), buffer);
            }
            return packet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int maxPacketSize(ConnectionState state) {
        return switch (state) {
            case HANDSHAKE, LOGIN -> ServerFlag.MAX_PACKET_SIZE_PRE_AUTH;
            default -> ServerFlag.MAX_PACKET_SIZE;
        };
    }
}
