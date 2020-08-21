package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryWriter;

/**
 * Class used to write packets
 */
public final class PacketUtils {

    private PacketUtils() {

    }

    /**
     * Write a {@link ServerPacket} into a {@link ByteBuf}
     *
     * @param buf    the recipient of {@code packet}
     * @param packet the packet to write into {@code buf}
     */
    public static void writePacket(ByteBuf buf, ServerPacket packet) {

        final ByteBuf packetBuffer = getPacketBuffer(packet);

        writePacket(buf, packetBuffer, packet.getId());
    }

    /**
     * Write a {@link ServerPacket} into a newly created {@link ByteBuf}
     *
     * @param packet the packet to write
     * @return a {@link ByteBuf} containing {@code packet}
     */
    public static ByteBuf writePacket(ServerPacket packet) {
        final ByteBuf packetBuffer = getPacketBuffer(packet);

        // Add 5 for the packet id and for the packet size
        final int size = packetBuffer.writerIndex() + 5 + 5;
        ByteBuf buffer = Unpooled.buffer(size);

        writePacket(buffer, packetBuffer, packet.getId());

        return buffer;
    }

    /**
     * Write a packet buffer into {@code buf}
     *
     * @param buf          the buffer which will receive the packet id/data
     * @param packetBuffer the buffer containing the raw packet data
     * @param packetId     the packet id
     */
    private static void writePacket(ByteBuf buf, ByteBuf packetBuffer, int packetId) {
        Utils.writeVarIntBuf(buf, packetId);
        buf.writeBytes(packetBuffer);
    }

    /**
     * Get the buffer representing the raw packet data
     *
     * @param packet the packet to write
     * @return the {@link ByteBuf} containing the raw packet data
     */
    private static ByteBuf getPacketBuffer(ServerPacket packet) {
        BinaryWriter writer = new BinaryWriter();
        packet.write(writer);

        return writer.getBuffer();
    }

}
