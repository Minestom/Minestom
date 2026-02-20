package net.minestom.server.network.packet;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketReading.class);

    private static final int MAX_VAR_INT_SIZE = 5;
    private static final Result.Empty EMPTY_CLIENT_PACKET = new Result.Empty<>();

    public sealed interface Result<T> {

        /**
         * At least one packet was read.
         * The buffer may still contain half-read packets and should therefore be compacted for next read.
         */
        record Success<T>(List<ParsedPacket<T>> packets) implements Result<T> {
            public Success {
                if (packets.isEmpty()) {
                    throw new IllegalArgumentException("Empty packets");
                }
                packets = List.copyOf(packets);
            }

            public Success(ParsedPacket<T> packet) {
                this(List.of(packet));
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

    public record ParsedPacket<T>(ConnectionState nextState, T packet) {
    }

    public static Result<ClientPacket> readClients(
            NetworkBuffer buffer,
            ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPackets(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, PacketVanilla::nextClientState, compressed);
    }

    public static Result<ServerPacket> readServers(
            NetworkBuffer buffer,
            ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPackets(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, PacketVanilla::nextServerState, compressed);
    }

    public static <T> Result<T> readPackets(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed
    ) throws DataFormatException {
        List<ParsedPacket<T>> packets = new ArrayList<>();
        readLoop:
        while (buffer.readableBytes() > 0) {
            final Result<T> result = readPacket(buffer, parser, state, stateUpdater, compressed);
            if (buffer.readableBytes() == 0 && packets.isEmpty()) return result;
            switch (result) {
                case Result.Success<T> success -> {
                    assert success.packets().size() == 1;
                    final ParsedPacket<T> parsedPacket = success.packets().getFirst();
                    packets.add(parsedPacket);
                    state = parsedPacket.nextState();
                }
                case Result.Empty<T> _ -> {
                    break readLoop;
                }
                case Result.Failure<T> failure -> {
                    return packets.isEmpty() ? failure : new Result.Success<>(packets);
                }
            }
        }
        return !packets.isEmpty() ? new Result.Success<>(packets) : EMPTY_CLIENT_PACKET;
    }

    public static Result<ClientPacket> readClient(
            NetworkBuffer buffer,
            ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPacket(buffer, PacketVanilla.CLIENT_PACKET_PARSER, state, PacketVanilla::nextClientState, compressed);
    }

    public static Result<ServerPacket> readServer(
            NetworkBuffer buffer,
            ConnectionState state,
            boolean compressed
    ) throws DataFormatException {
        return readPacket(buffer, PacketVanilla.SERVER_PACKET_PARSER, state, PacketVanilla::nextServerState, compressed);
    }

    public static <T> Result<T> readPacket(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed
    ) throws DataFormatException {
        final long beginMark = buffer.readIndex();
        // READ PACKET LENGTH
        final int packetLength;
        try {
            packetLength = buffer.read(VAR_INT);
        } catch (IndexOutOfBoundsException e) {
            // Couldn't read a single var-int
            buffer.readIndex(beginMark);
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
            throw new DataFormatException("Packet too large: %d > %d:%s".formatted(packetLength, maxPacketSize, state.name()));
        }
        // READ PAYLOAD https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Packet_format
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
        // We create a slice here so capacity is enforced, we also set it to read only cause we dont want readers writing into this buffer.
        final NetworkBuffer slice = buffer.slice(readerStart, packetLength, 0, packetLength).readOnly();
        final T packet = readFramedPacket(slice, parser, state, compressed);
        final ConnectionState nextState = stateUpdater.apply(packet, state);
        buffer.readIndex(readerEnd);
        return new Result.Success<>(new ParsedPacket<>(nextState, packet));
    }

    private static <T> T readFramedPacket(NetworkBuffer buffer,
                                          PacketParser<T> parser,
                                          ConnectionState state,
                                          boolean compressed) throws DataFormatException {
        if (!compressed) {
            // No compression format
            return readPayload(buffer, parser, state);
        }

        // READ COMPRESSION HEADER
        final int dataLength = buffer.read(VAR_INT);
        if (dataLength == 0) {
            // Uncompressed packet
            return readPayload(buffer, parser, state);
        }

        // Decompress the packet into the pooled buffer
        // and read the uncompressed packet from it
        NetworkBuffer decompressed = PacketVanilla.PACKET_POOL.get();
        try {
            if (decompressed.capacity() < dataLength) decompressed.resize(dataLength);
            buffer.decompress(buffer.readIndex(), buffer.readableBytes(), decompressed);
            return readPayload(decompressed.readOnly(), parser, state); // Payload should not write into the buffer
        } finally {
            PacketVanilla.PACKET_POOL.add(decompressed);
        }
    }

    private static <T> T readPayload(NetworkBuffer buffer, PacketParser<T> registry, ConnectionState state) {
        final int packetId = buffer.read(VAR_INT);
        final T packet = registry.parse(state, packetId, buffer);
        warnUnreadBytes(buffer, packet, packetId);
        return packet;
    }

    private static void warnUnreadBytes(NetworkBuffer buffer, Object packet, int packetId) {
        if (!ServerFlag.WARN_UNREAD_BYTES_PACKET || buffer.readableBytes() == 0) return;
        LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({})",
                packet.getClass().getSimpleName(), Integer.toHexString(packetId), buffer);
    }

    public static int maxPacketSize(ConnectionState state) {
        return switch (state) {
            case HANDSHAKE, LOGIN -> ServerFlag.MAX_PACKET_SIZE_PRE_AUTH;
            default -> ServerFlag.MAX_PACKET_SIZE;
        };
    }
}
