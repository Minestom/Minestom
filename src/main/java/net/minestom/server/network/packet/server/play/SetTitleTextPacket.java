package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetTitleTextPacket implements ServerPacket {

    public Component title = Component.empty();

    public SetTitleTextPacket() {
    }

    public SetTitleTextPacket(Component title) {
        this.title = title;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.title = reader.readComponent();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(title);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_TITLE_TEXT;
    }
}
