package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class OpenWindowPacket implements ServerPacket {

    public int windowId;
    public int windowType;
    public String title;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeVarInt(windowType);
        writer.writeSizedString("{\"text\": \"" + title + " \"}");
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_WINDOW;
    }
}
