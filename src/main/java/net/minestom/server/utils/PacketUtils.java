package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

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
        PacketWriter writer = new PacketWriter();

        Utils.writeVarIntBuf(buf, packet.getId());
        packet.write(writer);
        buf.writeBytes(writer.toByteArray());
    }

    /**
     * Write a {@link ServerPacket} into a newly created {@link ByteBuf}
     *
     * @param packet the packet to write
     * @return a {@link ByteBuf} containing {@code packet}
     */
    public static ByteBuf writePacket(ServerPacket packet) {
        ByteBuf buffer = Unpooled.buffer();

        writePacket(buffer, packet);

        return buffer;
    }

}
