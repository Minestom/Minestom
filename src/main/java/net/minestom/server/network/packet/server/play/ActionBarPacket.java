package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ActionBarPacket(@NotNull Component text) implements ServerPacket {
    public ActionBarPacket(BinaryReader reader) {
        this(reader.readComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(text);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACTION_BAR;
    }
}
