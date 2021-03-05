package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.callback.validator.PlayerValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.zip.Deflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuf}
 * for network processing.
 */
public final class PacketUtils {

    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();
    private static final ThreadLocal<Deflater> DEFLATER = ThreadLocal.withInitial(Deflater::new);

    private PacketUtils() {

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

        if (MinecraftServer.hasGroupedPacket()) {
            // Send grouped packet...
            final boolean success = PACKET_LISTENER_MANAGER.processServerPacket(packet, players);
            if (success) {
                final ByteBuf finalBuffer = createFramedPacket(packet, false);
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
                playerConnection.sendPacket(packet);
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
     * Writes a {@link ServerPacket} into a {@link ByteBuf}.
     *
     * @param buf    the recipient of {@code packet}
     * @param packet the packet to write into {@code buf}
     */
    public static void writePacket(@NotNull ByteBuf buf, @NotNull ServerPacket packet) {
        final ByteBuf packetBuffer = getPacketBuffer(packet);

        writePacket(buf, packetBuffer, packet.getId());
    }

    /**
     * Writes a {@link ServerPacket} into a newly created {@link ByteBuf}.
     *
     * @param packet the packet to write
     * @return a {@link ByteBuf} containing {@code packet}
     */
    @NotNull
    public static ByteBuf writePacket(@NotNull ServerPacket packet) {
        final ByteBuf packetBuffer = getPacketBuffer(packet);

        // Add 5 for the packet id and for the packet size
        final int size = packetBuffer.writerIndex() + 5 + 5;
        ByteBuf buffer = BufUtils.getBuffer(true, size);

        writePacket(buffer, packetBuffer, packet.getId());

        return buffer;
    }

    /**
     * Writes a packet buffer into {@code buf}.
     *
     * @param buf          the buffer which will receive the packet id/data
     * @param packetBuffer the buffer containing the raw packet data
     * @param packetId     the packet id
     */
    private static void writePacket(@NotNull ByteBuf buf, @NotNull ByteBuf packetBuffer, int packetId) {
        Utils.writeVarIntBuf(buf, packetId);
        buf.writeBytes(packetBuffer);
        packetBuffer.release();
    }

    /**
     * Gets the buffer representing the raw packet data.
     *
     * @param packet the packet to write
     * @return the {@link ByteBuf} containing the raw packet data
     */
    @NotNull
    private static ByteBuf getPacketBuffer(@NotNull ServerPacket packet) {
        BinaryWriter writer;
        if (packet.getId() == ServerPacketIdentifier.CHUNK_DATA || packet.getId() == ServerPacketIdentifier.UPDATE_LIGHT) {
            writer = new BinaryWriter(BufUtils.getBuffer(true, 40_000));
        } else {
            writer = new BinaryWriter(BufUtils.getBuffer(true));
        }

        try {
            packet.write(writer);
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return writer.getBuffer();
    }

    /**
     * Frames a buffer for it to be understood by a Minecraft client.
     * <p>
     * The content of {@code packetBuffer} can be either a compressed or uncompressed packet buffer,
     * it depends of it the client did receive a {@link net.minestom.server.network.packet.server.login.SetCompressionPacket} packet before.
     *
     * @param packetBuffer the buffer containing compressed or uncompressed packet data
     * @param frameTarget  the buffer which will receive the framed version of {@code from}
     */
    public static void frameBuffer(@NotNull ByteBuf packetBuffer, @NotNull ByteBuf frameTarget) {
        final int packetSize = packetBuffer.readableBytes();
        final int headerSize = Utils.getVarIntSize(packetSize);

        if (headerSize > 3) {
            throw new IllegalStateException("Unable to fit " + headerSize + " into 3");
        }

        frameTarget.ensureWritable(packetSize + headerSize);

        Utils.writeVarIntBuf(frameTarget, packetSize);
        frameTarget.writeBytes(packetBuffer, packetBuffer.readerIndex(), packetSize);
    }

    /**
     * Compress using zlib the content of a packet.
     * <p>
     * {@code packetBuffer} needs to be the packet content without any header (if you want to use it to write a Minecraft packet).
     *
     * @param deflater          the deflater for zlib compression
     * @param buffer            a cached buffer which will be used to store temporary the deflater output,
     *                          null if you prefer the buffer to be allocated dynamically when required
     * @param packetBuffer      the buffer containing all the packet fields
     * @param compressionTarget the buffer which will receive the compressed version of {@code packetBuffer}
     */
    public static void compressBuffer(@NotNull Deflater deflater, @Nullable byte[] buffer,
                                      @NotNull ByteBuf packetBuffer, @NotNull ByteBuf compressionTarget) {
        final int packetLength = packetBuffer.readableBytes();

        if (packetLength < MinecraftServer.getCompressionThreshold()) {
            Utils.writeVarIntBuf(compressionTarget, 0);
            compressionTarget.writeBytes(packetBuffer);
        } else {

            Utils.writeVarIntBuf(compressionTarget, packetLength);

            // Allocate buffer if not already
            byte[] output = buffer != null ? buffer : new byte[8192];

            deflater.setInput(packetBuffer.nioBuffer());
            deflater.finish();

            while (!deflater.finished()) {
                final int length = deflater.deflate(output);
                compressionTarget.writeBytes(output, 0, length);
            }

            deflater.reset();
        }
    }

    /**
     * Creates a "framed packet" (packet which can be send and understood by a Minecraft client)
     * from a server packet.
     * <p>
     * Can be used if you want to store a raw buffer and send it later without the additional writing cost.
     * Compression is applied if {@link MinecraftServer#getCompressionThreshold()} is greater than 0.
     *
     * @param serverPacket the server packet to write
     * @return the framed packet from the server one
     */
    @NotNull
    public static ByteBuf createFramedPacket(@NotNull ServerPacket serverPacket, boolean directBuffer) {
        ByteBuf packetBuf = writePacket(serverPacket);

        if (MinecraftServer.getCompressionThreshold() > 0) {
            ByteBuf compressedBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();
            ByteBuf framedBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();

            final Deflater deflater = DEFLATER.get();
            compressBuffer(deflater, null, packetBuf, compressedBuf);

            packetBuf.release();

            frameBuffer(compressedBuf, framedBuf);
            compressedBuf.release();

            return framedBuf;
        } else {
            ByteBuf framedBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();
            frameBuffer(packetBuf, framedBuf);
            packetBuf.release();

            return framedBuf;
        }

    }

}
