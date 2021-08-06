package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class OpenWindowPacket implements ServerPacket {

    public int windowId;
    public int windowType;
    public Component title = Component.text("");

    public OpenWindowPacket() {
    }

    public OpenWindowPacket(Component title) {
        this.title = title;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeVarInt(windowType);
        writer.writeComponent(title);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readVarInt();
        windowType = reader.readVarInt();
        title = reader.readComponent();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_WINDOW;
    }
}
