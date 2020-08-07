package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public final class PacketUtils {

    private PacketUtils() {

    }

    public static void writePacket(ByteBuf buf, ServerPacket packet) {
        PacketWriter writer = new PacketWriter();

        Utils.writeVarIntBuf(buf, packet.getId());
        packet.write(writer);
        buf.writeBytes(writer.toByteArray());
    }

    public static ByteBuf writePacket(ServerPacket packet) {
        ByteBuf buffer = Unpooled.buffer();

        writePacket(buffer, packet);

        return buffer;
    }

}
