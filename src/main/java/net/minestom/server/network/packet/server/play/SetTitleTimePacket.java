package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SetTitleTimePacket(int fadeIn, int stay, int fadeOut) implements ServerPacket {
    public SetTitleTimePacket(BinaryReader reader) {
        this(reader.readInt(), reader.readInt(), reader.readInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(fadeIn);
        writer.writeInt(stay);
        writer.writeInt(fadeOut);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_TITLE_TIME;
    }
}
