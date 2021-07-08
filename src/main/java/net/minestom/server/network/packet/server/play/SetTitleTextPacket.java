package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

public class SetTitleTextPacket implements ComponentHoldingServerPacket {

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

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(title);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetTitleTextPacket(operator.apply(title));
    }
}
