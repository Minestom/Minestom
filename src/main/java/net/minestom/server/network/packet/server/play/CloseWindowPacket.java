package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class CloseWindowPacket implements ServerPacket {

    public byte windowId;

    public CloseWindowPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CLOSE_WINDOW;
    }
}
