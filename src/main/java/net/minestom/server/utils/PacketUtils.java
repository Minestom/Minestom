package net.minestom.server.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.validator.PlayerValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.zip.Deflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuffer}
 * for network processing.
 */
public final class PacketUtils {
    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();
    private static final ThreadLocal<Deflater> COMPRESSOR = ThreadLocal.withInitial(Deflater::new);

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
                                         @Nullable PlayerValidator playerValidator) {
        if (players.isEmpty())
            return;

        // work out if the packet needs to be sent individually due to server-side translating
        boolean needsTranslating = false;
        if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ComponentHoldingServerPacket) {
            needsTranslating = ComponentUtils.areAnyTranslatable(((ComponentHoldingServerPacket) packet).components());
        }

        if (MinecraftServer.hasGroupedPacket() && !needsTranslating) {
            // Send grouped packet...
            final boolean success = PACKET_LISTENER_MANAGER.processServerPacket(packet, players);
            if (success) {
                final ByteBuffer finalBuffer = createFramedPacket(packet);
                final FramedPacket framedPacket = new FramedPacket(finalBuffer);
                // Send packet to all players
                for (Player player : players) {
                    if (!player.isOnline())
                        continue;
                    // Verify if the player should receive the packet
                    if (playerValidator != null && !playerValidator.isValid(player))
                        continue;

                    final PlayerConnection playerConnection = player.getPlayerConnection();
                    if (playerConnection instanceof NettyPlayerConnection) {
                        final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) playerConnection;
                        nettyPlayerConnection.write(framedPacket);
                    } else {
                        playerConnection.sendPacket(packet);
                    }
                }
            }
        } else {
            // Write the same packet for each individual players
            for (Player player : players) {
                // Verify if the player should receive the packet
                if (playerValidator != null && !playerValidator.isValid(player))
                    continue;
                final PlayerConnection playerConnection = player.getPlayerConnection();
                playerConnection.sendPacket(packet, false);
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
        sendGroupedPacket(players, packet, null);
    }

    /**
     * Writes a {@link ServerPacket} into a {@link ByteBuffer}.
     *
     * @param buf    the recipient of {@code packet}
     * @param packet the packet to write into {@code buf}
     */
    public static void writePacket(@NotNull ByteBuffer buf, @NotNull ServerPacket packet) {
        Utils.writeVarInt(buf, packet.getId());
        BinaryWriter writer = new BinaryWriter(buf);
        try {
            packet.write(writer);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    public static void writeFramedPacket(@NotNull ByteBuffer buffer,
                                         @NotNull ServerPacket serverPacket) {
        final int compressionThreshold = MinecraftServer.getCompressionThreshold();

        // Index of the var-int containing the complete packet length
        final int packetLengthIndex = Utils.writeEmptyVarIntHeader(buffer);
        final int startIndex = buffer.position(); // Index where the content starts (after length)
        if (compressionThreshold > 0) {
            // Index of the uncompressed payload length
            final int dataLengthIndex = Utils.writeEmptyVarIntHeader(buffer);

            // Write packet
            final int contentIndex = buffer.position();
            writePacket(buffer, serverPacket);
            final int packetSize = buffer.position() - contentIndex;

            final int uncompressedLength = packetSize >= compressionThreshold ? packetSize : 0;
            Utils.writeVarIntHeader(buffer, dataLengthIndex, uncompressedLength);
            if (uncompressedLength > 0) {
                // Packet large enough, compress
                ByteBuffer uncompressedCopy = buffer.duplicate().position(contentIndex).limit(contentIndex + packetSize);
                buffer.position(contentIndex);

                var deflater = COMPRESSOR.get();
                deflater.setInput(uncompressedCopy);
                deflater.finish();
                deflater.deflate(buffer);
                deflater.reset();
            }
        } else {
            // No compression, write packet id + payload
            writePacket(buffer, serverPacket);
        }
        // Total length
        final int totalPacketLength = buffer.position() - startIndex;
        Utils.writeVarIntHeader(buffer, packetLengthIndex, totalPacketLength);
    }

    /**
     * Creates a "framed packet" (packet which can be send and understood by a Minecraft client)
     * from a server packet, directly into an output buffer.
     * <p>
     * Can be used if you want to store a raw buffer and send it later without the additional writing cost.
     * Compression is applied if {@link MinecraftServer#getCompressionThreshold()} is greater than 0.
     */
    public static @NotNull ByteBuffer createFramedPacket(@NotNull ServerPacket serverPacket) {
        ByteBuffer packetBuf = ByteBuffer.allocate(2_000_000);
        writeFramedPacket(packetBuf, serverPacket);
        return packetBuf;
    }
}
