package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowConfirmationPacket implements ServerPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(actionNumber);
        writer.writeBoolean(accepted);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_CONFIRMATION;
    }
}
