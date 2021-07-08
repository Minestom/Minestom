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

public class SetTitleSubTitlePacket implements ComponentHoldingServerPacket {

    public Component subtitle = Component.empty();

    public SetTitleSubTitlePacket() {
    }

    public SetTitleSubTitlePacket(Component subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.subtitle = reader.readComponent();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeComponent(subtitle);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_TITLE_SUBTITLE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return Collections.singleton(subtitle);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetTitleSubTitlePacket(operator.apply(subtitle));
    }
}
