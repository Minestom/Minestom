package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowConfirmationPacket implements ServerPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    /**
     * Default constructor, required for reflection operations.
     */
    public WindowConfirmationPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(actionNumber);
        writer.writeBoolean(accepted);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
        actionNumber = reader.readShort();
        accepted = reader.readBoolean();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_CONFIRMATION;
    }
}
