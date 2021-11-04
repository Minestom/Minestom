package net.minestom.server.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.packet.FramedPacket;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.network.socket.Server;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.PooledBuffers;
import net.minestom.server.utils.cache.LocalCache;
import net.minestom.server.utils.callback.validator.PlayerValidator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.Deflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuffer}
 * for network processing.
 * <p>
 * Note that all methods are mostly internal and can change at any moment.
 * This is due to their very unsafe nature (use of local buffers as cache) and their potential performance impact.
 * Be sure to check the implementation code.
 */
public final class PacketUtils {
    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();
    private static final LocalCache<Deflater> LOCAL_DEFLATER = LocalCache.of(Deflater::new);

    /// Local buffers
    private static final LocalCache<ByteBuffer> PACKET_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE);
    private static final LocalCache<ByteBuffer> LOCAL_BUFFER = LocalCache.ofBuffer(Server.MAX_PACKET_SIZE);

    // Viewable packets
    private static final ConcurrentMap<Viewable, ViewableStorage> VIEWABLE_STORAGE_MAP = ConcurrentMap.class.cast(Caffeine.newBuilder().weakKeys().build().asMap());

    private PacketUtils() {
    }

    @ApiStatus.Internal
    @ApiStatus.Experimental
    public static ByteBuffer localBuffer() {
        return LOCAL_BUFFER.get().clear();
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
        if (audience instanceof Player) {
            ((Player) audience).getPlayerConnection().sendPacket(packet);
        } else if (audience instanceof PacketGroupingAudience) {
            PacketUtils.sendGroupedPacket(((PacketGroupingAudience) audience).getPlayers(), packet);
        } else if (audience instanceof ForwardingAudience.Single) {
            PacketUtils.sendPacket(((ForwardingAudience.Single) audience).audience(), packet);
        } else if (audience instanceof ForwardingAudience) {
            for (Audience member : ((ForwardingAudience) audience).audiences()) {
                PacketUtils.sendPacket(member, packet);
            }
        }
    }

    /**
     * Sends a {@link ServerPacket} to multiple players.
     * <p>
     * Can drastically improve performance since the packet will not have to be processed as much.
     *
     * @param players         the players to send the packet to
     * @param packet          the packet to send to the players
     * @param playerValidator optional callback to check if a specify player of {@code players} should receive the packet
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet,
                                         @NotNull PlayerValidator playerValidator) {
        if (players.isEmpty())
            return;
        // work out if the packet needs to be sent individually due to server-side translating
        boolean needsTranslating = false;
        if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ComponentHoldingServerPacket) {
            needsTranslating = ComponentUtils.areAnyTranslatable(((ComponentHoldingServerPacket) packet).components());
        }
        if (MinecraftServer.hasGroupedPacket() && !needsTranslating) {
            // Send grouped packet...
            if (!PACKET_LISTENER_MANAGER.processServerPacket(packet, players))
                return;
            final FramedPacket framedPacket = new FramedPacket(packet, createFramedPacket(packet));
            // Send packet to all players
            players.forEach(player -> {
                if (!player.isOnline() || !playerValidator.isValid(player))
                    return;
                player.sendPacket(framedPacket);
            });
        } else {
            // Write the same packet for each individual players
            players.forEach(player -> {
                if (!player.isOnline() || !playerValidator.isValid(player))
                    return;
                player.getPlayerConnection().sendPacket(packet, false);
            });
        }
    }

    /**
     * Same as {@link #sendGroupedPacket(Collection, ServerPacket, PlayerValidator)}
     * but with the player validator sets to null.
     *
     * @see #sendGroupedPacket(Collection, ServerPacket, PlayerValidator)
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet) {
        sendGroupedPacket(players, packet, player -> true);
    }

    public static void broadcastPacket(@NotNull ServerPacket packet) {
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
        final Player exception = entity instanceof Player ? (Player) entity : null;
        VIEWABLE_STORAGE_MAP.compute(viewable, (v, storage) -> {
            if (storage == null) storage = new ViewableStorage();
            storage.append(v, serverPacket, exception);
            return storage;
        });
    }

    @ApiStatus.Experimental
    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    @ApiStatus.Internal
    public static void flush() {
        VIEWABLE_STORAGE_MAP.entrySet().parallelStream().forEach(entry ->
                entry.getValue().process(entry.getKey()));
    }

    public static void writeFramedPacket(@NotNull ByteBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         boolean compression) {
        BinaryWriter writerView = BinaryWriter.view(buffer); // ensure that the buffer is not resized
        if (!compression) {
            // Uncompressed format https://wiki.vg/Protocol#Without_compression
            final int lengthIndex = Utils.writeEmptyVarIntHeader(buffer);
            Utils.writeVarInt(buffer, packet.getId());
            packet.write(writerView);
            final int finalSize = buffer.position() - (lengthIndex + 3);
            Utils.writeVarIntHeader(buffer, lengthIndex, finalSize);
            return;
        }
        // Compressed format https://wiki.vg/Protocol#With_compression
        final int compressedIndex = Utils.writeEmptyVarIntHeader(buffer);
        final int uncompressedIndex = Utils.writeEmptyVarIntHeader(buffer);

        final int contentStart = buffer.position();
        Utils.writeVarInt(buffer, packet.getId());
        packet.write(writerView);
        final int packetSize = buffer.position() - contentStart;
        final boolean compressed = packetSize >= MinecraftServer.getCompressionThreshold();
        if (compressed) {
            // Packet large enough, compress it
            final ByteBuffer input = localBuffer().put(0, buffer, contentStart, packetSize);
            Deflater deflater = LOCAL_DEFLATER.get();
            deflater.setInput(input.limit(packetSize));
            deflater.finish();
            deflater.deflate(buffer.position(contentStart));
            deflater.reset();
        }
        // Packet header (Packet + Data Length)
        Utils.writeVarIntHeader(buffer, compressedIndex, buffer.position() - uncompressedIndex);
        Utils.writeVarIntHeader(buffer, uncompressedIndex, compressed ? packetSize : 0);
    }

    @ApiStatus.Internal
    public static ByteBuffer createFramedPacket(@NotNull ServerPacket packet, boolean compression) {
        ByteBuffer buffer = PACKET_BUFFER.get().clear();
        writeFramedPacket(buffer, packet, compression);
        return buffer.flip();
    }

    @ApiStatus.Internal
    public static ByteBuffer createFramedPacket(@NotNull ServerPacket packet) {
        return createFramedPacket(packet, MinecraftServer.getCompressionThreshold() > 0);
    }

    @ApiStatus.Internal
    public static FramedPacket allocateTrimmedPacket(@NotNull ServerPacket packet) {
        final ByteBuffer temp = PacketUtils.createFramedPacket(packet);
        final int size = temp.remaining();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(size).put(0, temp, 0, size);
        return new FramedPacket(packet, buffer);
    }

    private static final class ViewableStorage {
        // Player id -> list of offsets to ignore (32:32 bits)
        private final Int2ObjectMap<LongArrayList> entityIdMap = new Int2ObjectOpenHashMap<>();
        private final BinaryBuffer buffer = PooledBuffers.get();

        {
            PooledBuffers.registerBuffer(this, buffer);
        }

        private void append(Viewable viewable, ServerPacket serverPacket, Player player) {
            final ByteBuffer framedPacket = createFramedPacket(serverPacket);
            final int packetSize = framedPacket.limit();
            if (packetSize >= buffer.capacity()) {
                process(viewable);
                for (Player viewer : viewable.getViewers()) {
                    if (!Objects.equals(player, viewer)) {
                        writeTo(viewer.getPlayerConnection(), framedPacket, 0, packetSize);
                    }
                }
                return;
            }
            if (!buffer.canWrite(packetSize)) process(viewable);
            final int start = buffer.writerOffset();
            this.buffer.write(framedPacket);
            final int end = buffer.writerOffset();
            if (player != null) {
                final long offsets = (long) start << 32 | end & 0xFFFFFFFFL;
                LongList list = entityIdMap.computeIfAbsent(player.getEntityId(), id -> new LongArrayList());
                list.add(offsets);
            }
        }

        private void process(Viewable viewable) {
            if (buffer.writerOffset() == 0) return;
            viewable.getViewers().forEach(this::processPlayer);
            this.buffer.clear();
            this.entityIdMap.clear();
        }

        private void processPlayer(Player player) {
            final int size = buffer.writerOffset();
            final PlayerConnection connection = player.getPlayerConnection();
            final LongArrayList pairs = entityIdMap.get(player.getEntityId());
            if (pairs != null) {
                // Ensure that we skip the specified parts of the buffer
                int lastWrite = 0;
                final long[] elements = pairs.elements();
                for (int i = 0; i < pairs.size(); ++i) {
                    final long offsets = elements[i];
                    final int start = (int) (offsets >> 32);
                    if (start != lastWrite) writeTo(connection, lastWrite, start - lastWrite);
                    lastWrite = (int) offsets; // End = last 32 bits
                }
                if (size != lastWrite) writeTo(connection, lastWrite, size - lastWrite);
            } else {
                // Write all
                writeTo(connection, 0, size);
            }
        }

        private void writeTo(PlayerConnection connection, int offset, int length) {
            writeTo(connection, buffer.asByteBuffer(), offset, length);
        }

        private static void writeTo(PlayerConnection connection, ByteBuffer buffer, int offset, int length) {
            if (connection instanceof PlayerSocketConnection socketConnection) {
                socketConnection.write(buffer, offset, length);
                return;
            }
            // TODO for non-socket connection
        }
    }
}
