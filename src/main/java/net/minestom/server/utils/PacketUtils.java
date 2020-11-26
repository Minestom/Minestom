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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.zip.Deflater;

/**
 * Utils class for packets. Including writing a {@link ServerPacket} into a {@link ByteBuf}
 * for network processing.
 */
public final class PacketUtils {

    private static final PacketListenerManager PACKET_LISTENER_MANAGER = MinecraftServer.getPacketListenerManager();

    private final static Deflater deflater = new Deflater(3);
    private final static byte[] buffer = new byte[8192];

    private PacketUtils() {

    }

    /**
     * Sends a {@link ServerPacket} to multiple players.
     * <p>
     * Can drastically improve performance since the packet will not have to be processed as much.
     *
     * @param players the players to send the packet to
     * @param packet  the packet to send to the players
     */
    public static void sendGroupedPacket(@NotNull Collection<Player> players, @NotNull ServerPacket packet) {
        if (players.isEmpty())
            return;

        final boolean success = PACKET_LISTENER_MANAGER.processServerPacket(packet, players);
        if (success) {
            final ByteBuf finalBuffer = createFramedPacket(packet, true);
            final FramedPacket framedPacket = new FramedPacket(finalBuffer);

            final int refIncrease = players.size() - 1;
            if (refIncrease > 0)
                finalBuffer.retain(refIncrease);
            for (Player player : players) {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                if (playerConnection instanceof NettyPlayerConnection) {
                    final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) playerConnection;
                    nettyPlayerConnection.getChannel().write(framedPacket).addListener((p) -> finalBuffer.release());
                } else {
                    playerConnection.sendPacket(packet);
                    finalBuffer.release();
                }
            }
        }
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
            e.printStackTrace();
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
     * @param buffer            a cached buffer which will be used to store temporary the deflater output
     * @param packetBuffer      the buffer containing all the packet fields
     * @param compressionTarget the buffer which will receive the compressed version of {@code packetBuffer}
     */
    public static void compressBuffer(@NotNull Deflater deflater, @NotNull byte[] buffer, @NotNull ByteBuf packetBuffer, @NotNull ByteBuf compressionTarget) {
        final int packetLength = packetBuffer.readableBytes();

        if (packetLength < MinecraftServer.getCompressionThreshold()) {
            Utils.writeVarIntBuf(compressionTarget, 0);
            compressionTarget.writeBytes(packetBuffer);
        } else {
            Utils.writeVarIntBuf(compressionTarget, packetLength);

            deflater.setInput(packetBuffer.nioBuffer());
            deflater.finish();

            while (!deflater.finished()) {
                final int length = deflater.deflate(buffer);
                compressionTarget.writeBytes(buffer, 0, length);
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
    public static ByteBuf createFramedPacket(@NotNull ServerPacket serverPacket, boolean directBuffer) {
        ByteBuf packetBuf = writePacket(serverPacket);

        if (MinecraftServer.getCompressionThreshold() > 0) {

            ByteBuf compressedBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();
            ByteBuf framedBuf = directBuffer ? BufUtils.getBuffer(true) : Unpooled.buffer();
            synchronized (deflater) {
                compressBuffer(deflater, buffer, packetBuf, compressedBuf);
            }
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
