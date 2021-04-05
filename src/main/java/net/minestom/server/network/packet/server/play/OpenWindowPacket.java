package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class OpenWindowPacket implements ServerPacket {

    public int windowId;
    public int windowType;
    public JsonMessage title = ColoredText.of("");

    public OpenWindowPacket() {}

    public OpenWindowPacket(String title) {
        this.title = ColoredText.of(title);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeVarInt(windowType);
        writer.writeJsonMessage(title);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readVarInt();
        windowType = reader.readVarInt();
        title = reader.readJsonMessage(Integer.MAX_VALUE);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_WINDOW;
    }
}
