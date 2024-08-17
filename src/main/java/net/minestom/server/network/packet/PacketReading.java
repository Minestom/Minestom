package net.minestom.server.network.packet;

import net.minestom.server.ServerFlag;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
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
public final class PacketReading {
    private final static Logger LOGGER = LoggerFactory.getLogger(PacketReading.class);

    public static ReadResult<ClientPacket> readClients(
            @NotNull ConnectionState state,
            @NotNull NetworkBuffer readBuffer, boolean compressed
    ) throws DataFormatException {
        return readPackets(PacketVanilla.CLIENT_PACKET_PARSER, state, PacketVanilla::nextClientState, readBuffer, compressed);
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
                if (packetLength > ServerFlag.MAX_PACKET_SIZE) {
                    throw new DataFormatException("Packet too large: " + packetLength);
                }
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
                        NetworkBuffer decompressed = PacketVanilla.PACKET_POOL.get();
                        try {
                            content.decompress(content.readIndex(), content.readableBytes(), decompressed);
                            packet = readUncompressedPacket(parser, state, decompressed);
                        } finally {
                            PacketVanilla.PACKET_POOL.add(decompressed);
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
        final NetworkBuffer.Type<T> serializer = packetInfo.serializer();
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

    public record ReadResult<T>(List<T> packets, ConnectionState newState, int missingLength) {
    }
}
