package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
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
import net.minestom.server.utils.callback.validator.PlayerValidator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.zip.Deflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuffer}
 * for network processing.
 */
public final class PacketUtils {
    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();
    private static final ThreadLocal<Deflater> COMPRESSOR = ThreadLocal.withInitial(Deflater::new);
    private static final LocalCache PACKET_BUFFER = LocalCache.get("packet-buffer", Server.SOCKET_BUFFER_SIZE);
    private static final LocalCache COMPRESSION_CACHE = LocalCache.get("compression-buffer", Server.SOCKET_BUFFER_SIZE);

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
            final ByteBuffer finalBuffer = createFramedPacket(packet);
            final FramedPacket framedPacket = new FramedPacket(packet.getId(), finalBuffer, packet);
            // Send packet to all players
            for (Player player : players) {
                if (!player.isOnline() || !playerValidator.isValid(player))
                    continue;
                final PlayerConnection connection = player.getPlayerConnection();
                if (connection instanceof PlayerSocketConnection) {
                    ((PlayerSocketConnection) connection).write(framedPacket);
                } else {
                    connection.sendPacket(packet);
                }
            }
        } else {
            // Write the same packet for each individual players
            for (Player player : players) {
                if (!player.isOnline() || !playerValidator.isValid(player))
                    continue;
                player.getPlayerConnection().sendPacket(packet, false);
            }
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

    public static void writeFramedPacket(@NotNull ByteBuffer buffer,
                                         @NotNull ServerPacket packet,
                                         boolean compression) {
        if (!compression) {
            // Length + payload
            final int lengthIndex = Utils.writeEmptyVarIntHeader(buffer);
            Utils.writeVarInt(buffer, packet.getId());
            packet.write(new BinaryWriter(buffer));
            final int finalSize = buffer.position() - (lengthIndex + 3);
            Utils.writeVarIntHeader(buffer, lengthIndex, finalSize);
            return;
        }
        // Compressed format
        final int compressedIndex = Utils.writeEmptyVarIntHeader(buffer);
        final int uncompressedIndex = Utils.writeEmptyVarIntHeader(buffer);
        final int contentStart = buffer.position();

        Utils.writeVarInt(buffer, packet.getId());
        packet.write(new BinaryWriter(buffer));
        final int packetSize = buffer.position() - contentStart;
        if (packetSize >= MinecraftServer.getCompressionThreshold()) {
            // Packet large enough, compress
            final int limitCache = buffer.limit();
            buffer.position(contentStart).limit(contentStart + packetSize);
            var uncompressedCopy = COMPRESSION_CACHE.get().put(buffer).flip();
            buffer.position(contentStart).limit(limitCache);

            var deflater = COMPRESSOR.get();
            deflater.setInput(uncompressedCopy);
            deflater.finish();
            deflater.deflate(buffer);
            deflater.reset();

            Utils.writeVarIntHeader(buffer, compressedIndex, (buffer.position() - contentStart) + 3);
            Utils.writeVarIntHeader(buffer, uncompressedIndex, packetSize);
        } else {
            Utils.writeVarIntHeader(buffer, compressedIndex, packetSize + 3);
            Utils.writeVarIntHeader(buffer, uncompressedIndex, 0);
        }
    }

    public static ByteBuffer createFramedPacket(@NotNull ByteBuffer initial, @NotNull ServerPacket packet,
                                                boolean compression) {
        var buffer = initial;
        try {
            writeFramedPacket(buffer, packet, compression);
        } catch (BufferOverflowException e) {
            // In the unlikely case where the packet is bigger than the default buffer size,
            // increase to the highest authorized buffer size using heap (for cheap allocation)
            buffer = ByteBuffer.allocate(Server.MAX_PACKET_SIZE);
            writeFramedPacket(buffer, packet, compression);
        }
        return buffer;
    }

    public static ByteBuffer createFramedPacket(@NotNull ByteBuffer initial, @NotNull ServerPacket packet) {
        return createFramedPacket(initial, packet, MinecraftServer.getCompressionThreshold() > 0);
    }

    public static ByteBuffer createFramedPacket(@NotNull ServerPacket packet) {
        return createFramedPacket(PACKET_BUFFER.get(), packet);
    }

    public static ByteBuffer createFramedPacket(@NotNull ServerPacket packet, boolean compression) {
        return createFramedPacket(PACKET_BUFFER.get(), packet, compression);
    }

    @ApiStatus.Internal
    public static FramedPacket allocateTrimmedPacket(@NotNull ServerPacket packet) {
        final var temp = PacketUtils.createFramedPacket(packet);
        final var buffer = ByteBuffer.allocateDirect(temp.position()).put(temp.flip()).asReadOnlyBuffer();
        return new FramedPacket(packet.getId(), buffer, packet);
    }

    @ApiStatus.Internal
    public static final class LocalCache {
        private static final Map<String, LocalCache> CACHES = new ConcurrentHashMap<>();

        private final String name;
        private final ThreadLocal<ByteBuffer> cache;

        private LocalCache(String name, int size) {
            this.name = name;
            this.cache = ThreadLocal.withInitial(() -> ByteBuffer.allocateDirect(size));
        }

        public static LocalCache get(String name, int size) {
            return CACHES.computeIfAbsent(name, s -> new LocalCache(name, size));
        }

        public String name() {
            return name;
        }

        public ByteBuffer get() {
            return cache.get().clear();
        }
    }

    private static final Map<Viewable, ViewableStorage> VIEWABLE_STORAGE_MAP = new ConcurrentHashMap<>();

    private static class ViewableStorage {
        private final Viewable viewable;
        private final Entry entry = new ViewableStorage.Entry();

        private ViewableStorage(Viewable viewable) {
            this.viewable = viewable;
        }

        private synchronized void append(PlayerConnection playerConnection, ServerPacket serverPacket) {
            BinaryBuffer buffer = entry.buffer;
            final ByteBuffer framedPacket = createFramedPacket(serverPacket).flip();
            if (!buffer.canWrite(framedPacket.limit())) process();
            final int start = buffer.writerOffset();
            buffer.write(framedPacket);
            final int end = buffer.writerOffset();
            if (playerConnection != null) {
                List<IntIntPair> list = entry.entityIdMap.computeIfAbsent(playerConnection, playerConnection1 -> new ArrayList<>());
                list.add(IntIntPair.of(start, end));
            }
        }

        private synchronized void process() {
            BinaryBuffer buffer = entry.buffer;

            final Set<Player> viewers = viewable.getViewers();
            if (viewers.isEmpty()) return;
            for (Player player : viewers) {
                PlayerConnection connection = player.getPlayerConnection();
                Consumer<ByteBuffer> writer = connection instanceof PlayerSocketConnection
                        ? ((PlayerSocketConnection) connection)::write :
                        byteBuffer -> {
                            // TODO for non-socket connection
                        };

                final List<IntIntPair> pairs = entry.entityIdMap.get(connection);
                if (pairs != null) {
                    // Player should not be sent at least one packet, ignore ranges
                    int lastWrite = 0;
                    for (IntIntPair pair : pairs) {
                        final int start = pair.leftInt();
                        if (start > lastWrite) {
                            ByteBuffer slice = buffer.asByteBuffer(lastWrite, start);
                            slice.position(slice.limit());
                            writer.accept(slice);
                        }
                        lastWrite = pair.rightInt();
                    }
                    // Write remaining
                    final int remaining = buffer.writerOffset() - lastWrite;
                    if (remaining > 0) {
                        ByteBuffer remainSlice = buffer.asByteBuffer(lastWrite, remaining);
                        remainSlice.position(remaining);
                        writer.accept(remainSlice);
                    }
                } else {
                    // Send full buffer
                    ByteBuffer result = buffer.asByteBuffer(0, buffer.writerOffset());
                    result.position(result.limit());
                    writer.accept(result);
                }
            }
            this.entry.reset();
        }

        private static class Entry {
            private final Map<PlayerConnection, List<IntIntPair>> entityIdMap = new ConcurrentHashMap<>();
            private final BinaryBuffer buffer = BinaryBuffer.ofSize(Server.SOCKET_BUFFER_SIZE);

            void reset() {
                this.entityIdMap.clear();
                this.buffer.clear();
            }
        }
    }

    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket,
                                             @Nullable Player player) {
        if (player != null && !player.isAutoViewable()) {
            // Operation cannot be optimized
            player.sendPacketToViewers(serverPacket);
            return;
        }
        final PlayerConnection playerConnection = player != null ? player.getPlayerConnection() : null;
        ViewableStorage viewableStorage = VIEWABLE_STORAGE_MAP.computeIfAbsent(viewable, c -> new ViewableStorage(viewable));
        viewableStorage.append(playerConnection, serverPacket);
    }

    public static void prepareViewablePacket(@NotNull Viewable viewable, @NotNull ServerPacket serverPacket) {
        prepareViewablePacket(viewable, serverPacket, null);
    }

    public static void flush() {
        // TODO clear map
        for (ViewableStorage viewableStorage : VIEWABLE_STORAGE_MAP.values()) {
            viewableStorage.process();
        }
    }
}
