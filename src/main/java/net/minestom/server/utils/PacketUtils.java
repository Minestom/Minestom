package net.minestom.server.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBuffer.Type;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
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
    private static final PacketParser.Server SERVER_PACKET_PARSER = new PacketParser.Server();

    public static final ObjectPool<NetworkBuffer> PACKET_POOL = new ObjectPool<>(
            () -> NetworkBuffer.resizableBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process()),
            buffer -> {
                buffer.clear();
                return buffer;
            });

    // Viewable packets
    private static final Cache<Viewable, ViewableStorage> VIEWABLE_STORAGE_MAP = Caffeine.newBuilder().weakKeys().build();

    private PacketUtils() {
    }

    /**
     * Sends a packet to an audience. This method performs the following steps in the
     * following order:
     * <ol>
     *     <li>If {@code audience} is a {@link Player}, send the packet to them.</li>
     *     <li>Otherwise, if {@code audience} is a {@link PacketGroupingAudience}, call
     *     {@link #sendGroupedPacket(Collection, ServerPacket)} on the players that the
     *     grouping audience contains.</li>
     *     <li>Otherwise, if {@code audience} is a {@link ForwardingAudience.Single},
     *     call this method on the single audience inside the forwarding audience.</li>
     *     <li>Otherwise, if {@code audience} is a {@link ForwardingAudience}, call this
     *     method for each audience member of the forwarding audience.</li>
     *     <li>Otherwise, do nothing.</li>
     * </ol>
     *
     * @param audience the audience
     * @param packet   the packet
     */
    @SuppressWarnings("OverrideOnly") // we need to access the audiences inside ForwardingAudience
    public static void sendPacket(@NotNull Audience audience, @NotNull ServerPacket packet) {
        if (audience instanceof Player player) {
            player.sendPacket(packet);
        } else if (audience instanceof PacketGroupingAudience groupingAudience) {
            PacketUtils.sendGroupedPacket(groupingAudience.getPlayers(), packet);
        } else if (audience instanceof ForwardingAudience.Single singleAudience) {
            PacketUtils.sendPacket(singleAudience.audience(), packet);
        } else if (audience instanceof ForwardingAudience forwardingAudience) {
            for (Audience member : forwardingAudience.audiences()) {
                PacketUtils.sendPacket(member, packet);
            }
        }
    }

    /**
     * Sends a {@link ServerPacket} to multiple players.
     * <p>
     * Can drastically improve performance since the packet will not have to be processed as much.
     *
     * @param players   the players to send the packet to
     * @param packet    the packet to send to the players
     * @param predicate predicate to ignore specific players
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet,
                                         @NotNull Predicate<Player> predicate) {
        final var sendablePacket = shouldUseCachePacket(packet) ? new CachedPacket(packet) : packet;

        players.forEach(player -> {
            if (predicate.test(player)) player.sendPacket(sendablePacket);
        });
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

    /**
     * Same as {@link #sendGroupedPacket(Collection, ServerPacket, Predicate)}
     * but with the player validator sets to null.
     *
     * @see #sendGroupedPacket(Collection, ServerPacket, Predicate)
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet) {
        sendGroupedPacket(players, packet, player -> true);
    }

    public static void broadcastPlayPacket(@NotNull ServerPacket packet) {
        sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), packet);
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket,
                                             @Nullable Entity entity) {
        if (entity != null && !entity.hasPredictableViewers()) {
            // Operation cannot be optimized
            entity.sendPacketToViewers(serverPacket);
            return;
        }
        if (!ServerFlag.VIEWABLE_PACKET) {
            sendGroupedPacket(viewable.getViewers(), serverPacket, value -> !Objects.equals(value, entity));
            return;
        }
        final Player exception = entity instanceof Player ? (Player) entity : null;
        ViewableStorage storage = VIEWABLE_STORAGE_MAP.get(viewable, (unused) -> new ViewableStorage());
        storage.append(serverPacket, exception);
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    @ApiStatus.Internal
    public static void flush() {
        if (ServerFlag.VIEWABLE_PACKET) {
            VIEWABLE_STORAGE_MAP.asMap().entrySet().parallelStream().forEach(entry ->
                    entry.getValue().process(entry.getKey()));
        }
    }

    @ApiStatus.Internal
    public static int readPackets(@NotNull NetworkBuffer readBuffer, boolean compressed,
                                  BiConsumer<Integer, NetworkBuffer> payloadConsumer) throws DataFormatException {
        while (readBuffer.readableBytes() > 0) {
            final int beginMark = readBuffer.readIndex();
            try {
                // Ensure that the buffer contains the full packet (or wait for next socket read)
                final int packetLength = readBuffer.read(VAR_INT);
                if (readBuffer.readIndex() > readBuffer.writeIndex()) {
                    // Can't read the packet length
                    readBuffer.readIndex(beginMark);
                    return 0;
                }
                final int readerStart = readBuffer.readIndex();
                if (readBuffer.readableBytes() < packetLength) {
                    // Can't read the full packet
                    final int missingLength = packetLength - readBuffer.readableBytes();
                    readBuffer.readIndex(beginMark);
                    return missingLength;
                }
                // Read packet https://wiki.vg/Protocol#Packet_format
                NetworkBuffer content = readBuffer.slice(readBuffer.readIndex(), packetLength);
                if (compressed) {
                    final int dataLength = content.read(VAR_INT);
                    if (dataLength > 0) {
                        NetworkBuffer decompressed = PACKET_POOL.get();
                        try {
                            content.decompress(content.readIndex(), content.readableBytes(), decompressed);
                            readUncompressedPacket(decompressed, payloadConsumer);
                        } finally {
                            PACKET_POOL.add(decompressed);
                        }
                    } else {
                        readUncompressedPacket(content, payloadConsumer);
                    }
                } else {
                    readUncompressedPacket(content, payloadConsumer);
                }
                // Position buffer to read the next packet
                readBuffer.readIndex(readerStart + packetLength);
            } catch (BufferUnderflowException e) {
                readBuffer.readIndex(beginMark);
                return 0;
            }
        }
        return 0;
    }

    private static void readUncompressedPacket(@NotNull NetworkBuffer buffer, BiConsumer<Integer, NetworkBuffer> payloadConsumer) {
        final int packetId = buffer.read(VAR_INT);
        try {
            payloadConsumer.accept(packetId, buffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFramedPacket(@NotNull ConnectionState state, @NotNull NetworkBuffer buffer, @NotNull ServerPacket packet) {
        writeFramedPacket(state, buffer, packet, MinecraftServer.getCompressionThreshold() > 0);
    }

    public static void writeFramedPacket(@NotNull ConnectionState state,
                                         @NotNull NetworkBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         boolean compression) {
        final PacketRegistry<ServerPacket> registry = SERVER_PACKET_PARSER.stateRegistry(state);
        final PacketRegistry.PacketInfo<ServerPacket> packetInfo = registry.packetInfo(packet.getClass());
        final int id = packetInfo.id();
        final Type<ServerPacket> serializer = packetInfo.serializer();
        writeFramedPacket(
                buffer, serializer,
                id, packet,
                compression ? MinecraftServer.getCompressionThreshold() : 0
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
        type.write(buffer, packet);
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
        type.write(buffer, packet);
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
            writeFramedPacket(state, buffer, packet);
            final NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            return new FramedPacket(packet, copy);
        }
    }

    private static final class ViewableStorage {
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<LongArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final NetworkBuffer buffer = PACKET_POOL.getAndRegister(this);

        private synchronized void append(ServerPacket serverPacket, @Nullable Player exception) {
            final int start = buffer.writeIndex();
            // Viewable storage is only used for play packets, so fine to assume this.
            writeFramedPacket(ConnectionState.PLAY, buffer, serverPacket);
            final int end = buffer.writeIndex();
            if (exception != null) {
                final long offsets = (long) start << 32 | end & 0xFFFFFFFFL;
                LongList list = entityIdMap.computeIfAbsent(exception.getEntityId(), id -> new LongArrayList());
                list.add(offsets);
            }
        }

        private synchronized void process(Viewable viewable) {
            if (buffer.writeIndex() == 0) return;
            NetworkBuffer copy = buffer.copy(0, buffer.writeIndex());
            viewable.getViewers().forEach(player -> processPlayer(player, copy));
            this.buffer.clear();
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player, NetworkBuffer buffer) {
            final int size = buffer.size();
            final PlayerConnection connection = player.getPlayerConnection();
            final LongArrayList pairs = entityIdMap.get(player.getEntityId());
            if (pairs != null) {
                // Ensure that we skip the specified parts of the buffer
                int lastWrite = 0;
                final long[] elements = pairs.elements();
                for (int i = 0; i < pairs.size(); ++i) {
                    final long offsets = elements[i];
                    final int start = (int) (offsets >> 32);
                    if (start != lastWrite) writeTo(connection, buffer, lastWrite, start - lastWrite);
                    lastWrite = (int) offsets; // End = last 32 bits
                }
                if (size != lastWrite) writeTo(connection, buffer, lastWrite, size - lastWrite);
            } else {
                // Write all
                writeTo(connection, buffer, 0, size);
            }
        }

        private static void writeTo(PlayerConnection connection, NetworkBuffer buffer, int offset, int length) {
            if (connection instanceof PlayerSocketConnection socketConnection) {
                socketConnection.write(buffer, offset, length);
                return;
            }
            // TODO for non-socket connection
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
