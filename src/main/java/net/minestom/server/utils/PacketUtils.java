package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.manager.PacketListenerManager;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ServerPacket;
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

    private static Deflater deflater = new Deflater();
    private static byte[] buffer = new byte[8192];

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
        final boolean success = PACKET_LISTENER_MANAGER.processServerPacket(packet, players);
        if (success) {
            final ByteBuf finalBuffer = createFramedPacket(packet);
            final FramedPacket framedPacket = new FramedPacket(finalBuffer);

            for (Player player : players) {
                final PlayerConnection playerConnection = player.getPlayerConnection();
                if (playerConnection instanceof NettyPlayerConnection) {
                    final NettyPlayerConnection nettyPlayerConnection = (NettyPlayerConnection) playerConnection;
                    nettyPlayerConnection.getChannel().write(framedPacket);
                } else {
                    playerConnection.sendPacket(packet);
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
        ByteBuf buffer = Unpooled.buffer(size);

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
        BinaryWriter writer = new BinaryWriter();
        packet.write(writer);

        return writer.getBuffer();
    }

    public static void frameBuffer(@NotNull ByteBuf from, @NotNull ByteBuf to) {
        final int packetSize = from.readableBytes();
        final int headerSize = Utils.getVarIntSize(packetSize);

        if (headerSize > 3) {
            throw new IllegalStateException("Unable to fit " + headerSize + " into 3");
        }

        to.ensureWritable(packetSize + headerSize);

        Utils.writeVarIntBuf(to, packetSize);
        to.writeBytes(from, from.readerIndex(), packetSize);
    }

    public static void compressBuffer(@NotNull Deflater deflater, @NotNull byte[] buffer, @NotNull ByteBuf from, @NotNull ByteBuf to) {
        final int packetLength = from.readableBytes();

        if (packetLength < MinecraftServer.getCompressionThreshold()) {
            Utils.writeVarIntBuf(to, 0);
            to.writeBytes(from);
        } else {
            Utils.writeVarIntBuf(to, packetLength);

            deflater.setInput(from.nioBuffer());
            deflater.finish();

            while (!deflater.finished()) {
                final int length = deflater.deflate(buffer);
                to.writeBytes(buffer, 0, length);
            }

            deflater.reset();
        }
    }

    private static ByteBuf createFramedPacket(@NotNull ServerPacket serverPacket) {
        ByteBuf packetBuf = writePacket(serverPacket);

        // TODO use pooled buffers instead of unpooled ones
        if (MinecraftServer.getCompressionThreshold() > 0) {

            ByteBuf compressedBuf = Unpooled.buffer();
            ByteBuf framedBuf = Unpooled.buffer();
            synchronized (deflater) {
                compressBuffer(deflater, buffer, packetBuf, compressedBuf);
            }

            frameBuffer(compressedBuf, framedBuf);

            return framedBuf;
        } else {
            ByteBuf framedBuf = Unpooled.buffer();
            frameBuffer(packetBuf, framedBuf);

            return framedBuf;
        }

    }

}
