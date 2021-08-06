package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetTitleTimePacket implements ServerPacket {

    public int fadeIn;
    public int stay;
    public int fadeOut;

    public SetTitleTimePacket() {
    }

    public SetTitleTimePacket(int fadeIn, int stay, int fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.fadeIn = reader.readInt();
        this.stay = reader.readInt();
        this.fadeOut = reader.readInt();

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
