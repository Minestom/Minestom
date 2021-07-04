package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ActionBarPacket implements ServerPacket {

    public Component actionBarText;

    public ActionBarPacket(@NotNull Component actionBarText) {
        this.actionBarText = actionBarText;
    }

    public ActionBarPacket() {
        this(Component.empty());
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.actionBarText = reader.readComponent();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(actionBarText);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ACTION_BAR;
    }
}
