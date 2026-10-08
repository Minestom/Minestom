package net.minestom.server.network.packet;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.property.ServerProperties;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
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
    private static final Result.Empty<?> EMPTY_CLIENT_PACKET = new Result.Empty<>();
    private static final Result.Skipped<?> SKIPPED_CLIENT_PACKET = new Result.Skipped<>();

    @SuppressWarnings("unchecked")
    private static <T> Result<T> emptyResult() {
        return (Result<T>) EMPTY_CLIENT_PACKET;
    }

    @SuppressWarnings("unchecked")
    private static <T> Result<T> skippedResult() {
        return (Result<T>) SKIPPED_CLIENT_PACKET;
    }

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
         * Represents one or more complete packets that were intentionally skipped.
         */
        record Skipped<T>() implements Result<T> {
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

    @FunctionalInterface
    public interface PacketReader<T> {
        /**
         * Reads or skips a packet payload.
         *
         * @return the decoded packet, or {@code null} to skip the remainder of the current packet
         */
        @Nullable T read(PacketRegistry.PacketInfo<? extends T> packetInfo, NetworkBuffer buffer);
    }

    /**
     * Raw-frame hook applied pre-read on the decompressed slice.
     * <p>
     * Only invoked when {@link #intercepts} accepts the peeked id, so unsubscribed
     * packets stay on the zero-overhead vanilla path.
     * <p>
     * Internal plumbing, direct use is discouraged.
     */
    @ApiStatus.Internal
    public interface FrameHook {
        /**
         * Filters packets by id, before any decode.
         *
         * @param packetId the peeked packet id
         * @return true to invoke {@link #translate}, false for the vanilla path
         */
        default boolean intercepts(int packetId) {
            return true;
        }

        /**
         * Translates the given frames, returning zero or more frames to continue with.
         * <p>
         * Frames that are never returned are skipped.
         *
         * @param state the connection state the frames were received in
         * @param frames the incoming frames
         * @return the translated frames, in order
         */
        List<NetworkBuffer> translate(ConnectionState state, List<NetworkBuffer> frames);
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
        return readPackets(buffer, parser, state, stateUpdater, compressed, PacketReading::readPacketPayload);
    }

    public static <T> Result<T> readPackets(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed,
            PacketReader<T> packetReader
    ) throws DataFormatException {
        return readPackets(buffer, parser, state, stateUpdater, compressed, packetReader, null);
    }

    public static <T> Result<T> readPackets(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed,
            PacketReader<T> packetReader,
            @Nullable FrameHook frameHook
    ) throws DataFormatException {
        List<ParsedPacket<T>> packets = new ArrayList<>();
        boolean skipped = false;

        readLoop: while (buffer.readableBytes() > 0) {
            final Result<T> result = readPacket(buffer, parser, state, stateUpdater, compressed, packetReader, frameHook);

            if (buffer.readableBytes() == 0 && packets.isEmpty()) return result;

            switch (result) {
                case Result.Success<T> success -> {
                    packets.addAll(success.packets());
                    state = success.packets().getLast().nextState();
                }
                case Result.Skipped<T> _ -> skipped = true;
                case Result.Empty<T> _ -> {
                    break readLoop;
                }
                case Result.Failure<T> failure -> {
                    return packets.isEmpty() ? failure : new Result.Success<>(packets);
                }
            }
        }

        if (!packets.isEmpty()) return new Result.Success<>(packets);

        return skipped ? skippedResult() : emptyResult();
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
        return readPacket(buffer, parser, state, stateUpdater, compressed, PacketReading::readPacketPayload);
    }

    public static <T> Result<T> readPacket(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed,
            PacketReader<T> packetReader
    ) throws DataFormatException {
        return readPacket(buffer, parser, state, stateUpdater, compressed, packetReader, null);
    }

    public static <T> Result<T> readPacket(
            NetworkBuffer buffer,
            PacketParser<T> parser,
            ConnectionState state,
            BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
            boolean compressed,
            PacketReader<T> packetReader,
            @Nullable FrameHook frameHook
    ) throws DataFormatException {
        final long beginMark = buffer.readIndex();
        // READ PACKET LENGTH
        final int packetLength;
        try {
            packetLength = buffer.read(VAR_INT);
        } catch (IndexOutOfBoundsException _) {
            // Couldn't read a single var-int
            return new Result.Failure<>(MAX_VAR_INT_SIZE);
        }
        final long readerStart = buffer.readIndex();
        if (readerStart > buffer.writeIndex()) {
            // Can't read the packet length, buffer has enough capacity
            buffer.readIndex(beginMark);
            return emptyResult();
        }
        final int maxPacketSize = maxPacketSize(state);
        if (packetLength < 0) throw new DataFormatException("Packet length negative: " + packetLength);
        if (packetLength > maxPacketSize) throw new DataFormatException("Packet too large: " + packetLength);
        // READ PAYLOAD https://minecraft.wiki/w/Minecraft_Wiki:Projects/wiki.vg_merge/Protocol#Packet_format
        if (buffer.readableBytes() < packetLength) {
            // Can't read the full packet
            buffer.readIndex(beginMark);
            final long packetLengthVarIntSize = readerStart - beginMark;
            final long requiredCapacity = packetLengthVarIntSize + packetLength;
            // Must return a failure if the buffer is too small
            // Otherwise do nothing, and hope to read the packet remains next time
            if (requiredCapacity > buffer.capacity()) return new Result.Failure<>(requiredCapacity);
            else return emptyResult();
        }
        final long offset = buffer.advanceRead(packetLength); // ensureReadable checked above
        final NetworkBuffer wire = buffer.slice(offset, packetLength, 0, packetLength).readOnly();
        return readWirePayload(wire, parser, state, stateUpdater, compressed, packetReader, frameHook);
    }

    /**
     * Parses one wire frame: decompress, pre-read hook, decode.
     * <p>
     * A single wire packet can yield zero, one or several parsed packets. Translated
     * frames are decoded in order with chained states.
     *
     * @param wire the {@code [dataLength?][packetId + payload]} slice
     */
    private static <T> Result<T> readWirePayload(NetworkBuffer wire,
                                                 PacketParser<T> parser,
                                                 ConnectionState state,
                                                 BiFunction<T, ConnectionState, ConnectionState> stateUpdater,
                                                 boolean compressed,
                                                 PacketReader<T> packetReader,
                                                 @Nullable FrameHook frameHook
    ) throws DataFormatException {
        final int maxPacketSize = maxPacketSize(state);
        // With compression the wire frame is [dataLength][id + payload],
        // so the packet id is only visible on the decompressed payload.
        NetworkBuffer pooled = null;
        final NetworkBuffer payload;
        try {
            if (!compressed) {
                payload = wire;
            } else {
                final int dataLength = wire.read(VAR_INT);
                if (dataLength == 0) {
                    payload = wire;
                } else {
                    if (dataLength < 0 || dataLength > maxPacketSize)
                        throw new DataFormatException("Invalid decompressed length: " + dataLength);
                    pooled = PacketVanilla.PACKET_POOL.get();
                    if (pooled.capacity() < dataLength) pooled.resize(dataLength);
                    final NetworkBuffer slice = pooled.slice(0, dataLength, 0, 0);
                    slice.registries(wire.registries());
                    final long written = wire.decompress(wire.readIndex(), wire.readableBytes(), slice);
                    if (written != dataLength)
                        throw new DataFormatException("Decompressed length mismatch: expected " + dataLength + ", got " + written);
                    payload = slice.readOnly();
                }
            }

            // Pre-read hook on the decompressed [packetId + payload]. Null hook = vanilla path.
            List<NetworkBuffer> frames;
            if (frameHook != null) {
                final int peekedId;
                try {
                    peekedId = payload.readAt(payload.readIndex(), VAR_INT);
                } catch (IndexOutOfBoundsException _) {
                    return skippedResult();
                }
                if (frameHook.intercepts(peekedId)) {
                    // Mutable copy rebased to zero: copy() keeps absolute indexes
                    NetworkBuffer mutable = payload.copy(payload.readIndex(), payload.readableBytes(),
                            0, payload.readableBytes());
                    frames = frameHook.translate(state, List.of(mutable));
                } else {
                    frames = List.of(payload);
                }
            } else {
                frames = List.of(payload);
            }

            List<ParsedPacket<T>> out = new ArrayList<>();
            ConnectionState cursor = state;

            for (NetworkBuffer frame : frames) {
                final PacketRegistry<? extends T> registry = parser.stateRegistry(cursor);
                final @Nullable T packet = readPayload(frame.readOnly(), registry, packetReader);

                if (packet == null) continue;

                cursor = stateUpdater.apply(packet, cursor);
                out.add(new ParsedPacket<>(cursor, packet));
            }

            if (out.isEmpty()) return skippedResult();
            if (out.size() == 1) return new Result.Success<>(out.getFirst());

            return new Result.Success<>(out);
        } finally {
            if (pooled != null) PacketVanilla.PACKET_POOL.add(pooled);
        }
    }

    private static <T> @Nullable T readPayload(NetworkBuffer buffer, PacketRegistry<? extends T> registry,
                                               PacketReader<T> packetReader) {
        final int packetId = buffer.read(VAR_INT);
        final PacketRegistry.PacketInfo<? extends T> packetInfo = registry.packetInfo(packetId);
        try {
            final @Nullable T packet = packetReader.read(packetInfo, buffer);
            if (packet == null) {
                buffer.readIndex(buffer.writeIndex());
                return null;
            }
            if (ServerProperties.WARN_PACKET_UNREAD_BYTES.get() && buffer.readableBytes() != 0) {
                LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({})",
                        packetInfo.packetClass().getSimpleName(), Integer.toHexString(packetId), buffer);
            }
            return packet;
        } catch (Exception e) {
            throw new RuntimeException("failed to read packet " + packetInfo.packetClass(), e);
        }
    }

    private static <T> T readPacketPayload(PacketRegistry.PacketInfo<? extends T> packetInfo, NetworkBuffer buffer) {
        return packetInfo.serializer().read(buffer);
    }

    public static int maxPacketSize(ConnectionState state) {
        return switch (state) {
            case HANDSHAKE, STATUS, LOGIN -> ServerProperties.MAX_PACKET_SIZE_PRE_AUTH.get();
            default -> ServerProperties.MAX_PACKET_SIZE.get();
        };
    }
}
