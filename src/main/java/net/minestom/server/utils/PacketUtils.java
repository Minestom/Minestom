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
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketRegistry;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.binary.BinaryBuffer;
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
    private static final ThreadLocal<Deflater> LOCAL_DEFLATER = ThreadLocal.withInitial(Deflater::new);


    private static final PacketParser.Server SERVER_PACKET_PARSER = new PacketParser.Server();

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
     * @see PlayerSocketConnection#writePacketSync(SendablePacket, boolean)
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
        storage.append(viewable, serverPacket, exception);
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
    public static @Nullable BinaryBuffer readPackets(@NotNull BinaryBuffer readBuffer, boolean compressed,
                                                     BiConsumer<Integer, ByteBuffer> payloadConsumer) throws DataFormatException {
        BinaryBuffer remaining = null;
        ByteBuffer pool = ObjectPool.PACKET_POOL.get();
        while (readBuffer.readableBytes() > 0) {
            final var beginMark = readBuffer.mark();
            try {
                // Ensure that the buffer contains the full packet (or wait for next socket read)
                final int packetLength = readBuffer.readVarInt();
                final int readerStart = readBuffer.readerOffset();
                if (!readBuffer.canRead(packetLength)) {
                    // Integrity fail
                    throw new BufferUnderflowException();
                }
                // Read packet https://wiki.vg/Protocol#Packet_format
                BinaryBuffer content = readBuffer;
                int decompressedSize = packetLength;
                if (compressed) {
                    final int dataLength = readBuffer.readVarInt();
                    final int payloadLength = packetLength - (readBuffer.readerOffset() - readerStart);
                    if (payloadLength < 0) {
                        throw new DataFormatException("Negative payload length " + payloadLength);
                    }
                    if (dataLength == 0) {
                        // Data is too small to be compressed, payload is following
                        decompressedSize = payloadLength;
                    } else {
                        // Decompress to content buffer
                        content = BinaryBuffer.wrap(pool);
                        decompressedSize = dataLength;
                        Inflater inflater = new Inflater(); // TODO: Pool?
                        inflater.setInput(readBuffer.asByteBuffer(readBuffer.readerOffset(), payloadLength));
                        inflater.inflate(content.asByteBuffer(0, dataLength));
                        inflater.reset();
                    }
                }
                // Slice packet
                ByteBuffer payload = content.asByteBuffer(content.readerOffset(), decompressedSize);
                final int packetId = Utils.readVarInt(payload);
                try {
                    payloadConsumer.accept(packetId, payload);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // Position buffer to read the next packet
                readBuffer.readerOffset(readerStart + packetLength);
            } catch (BufferUnderflowException e) {
                readBuffer.reset(beginMark);
                remaining = BinaryBuffer.copy(readBuffer);
                break;
            }
        }
        ObjectPool.PACKET_POOL.add(pool);
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
            Utils.writeVarIntHeader(buffer, lengthIndex, finalSize);
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
            try (var hold = ObjectPool.PACKET_POOL.hold()) {
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
        Utils.writeVarIntHeader(buffer, compressedIndex, networkBuffer.writeIndex() - uncompressedIndex);
        Utils.writeVarIntHeader(buffer, uncompressedIndex, compressed ? packetSize : 0);

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
        try (var hold = ObjectPool.PACKET_POOL.hold()) {
            final ByteBuffer temp = PacketUtils.createFramedPacket(state, hold.get(), packet);
            final int size = temp.remaining();
            final ByteBuffer buffer = ByteBuffer.allocateDirect(size).put(0, temp, 0, size);
            return new FramedPacket(packet, buffer);
        }
    }

    private static final class ViewableStorage {
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<LongArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final BinaryBuffer buffer = ObjectPool.BUFFER_POOL.getAndRegister(this);

        private synchronized void append(Viewable viewable, ServerPacket serverPacket, @Nullable Player exception) {
            try (var hold = ObjectPool.PACKET_POOL.hold()) {
                // Viewable storage is only used for play packets, so fine to assume this.
                final ByteBuffer framedPacket = createFramedPacket(ConnectionState.PLAY, hold.get(), serverPacket);
                final int packetSize = framedPacket.limit();
                if (packetSize >= buffer.capacity()) {
                    process(viewable);
                    for (Player viewer : viewable.getViewers()) {
                        if (!Objects.equals(exception, viewer)) {
                            writeTo(viewer.getPlayerConnection(), framedPacket, 0, packetSize);
                        }
                    }
                    return;
                }
                if (!buffer.canWrite(packetSize)) process(viewable);
                final int start = buffer.writerOffset();
                this.buffer.write(framedPacket);
                final int end = buffer.writerOffset();
                if (exception != null) {
                    final long offsets = (long) start << 32 | end & 0xFFFFFFFFL;
                    LongList list = entityIdMap.computeIfAbsent(exception.getEntityId(), id -> new LongArrayList());
                    list.add(offsets);
                }
            }
        }

        private synchronized void process(Viewable viewable) {
            if (buffer.writerOffset() == 0) return;
            ByteBuffer copy = ByteBuffer.allocateDirect(buffer.writerOffset());
            copy.put(buffer.asByteBuffer(0, copy.capacity()));
            viewable.getViewers().forEach(player -> processPlayer(player, copy));
            this.buffer.clear();
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player, ByteBuffer buffer) {
            final int size = buffer.limit();
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

        private static void writeTo(PlayerConnection connection, ByteBuffer buffer, int offset, int length) {
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
