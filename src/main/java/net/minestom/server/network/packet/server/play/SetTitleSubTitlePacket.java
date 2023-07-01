package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record SetTitleSubTitlePacket(@NotNull Component subtitle) implements ComponentHoldingServerPacket {
    public SetTitleSubTitlePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(COMPONENT, subtitle);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_TITLE_SUBTITLE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.subtitle);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SetTitleSubTitlePacket(operator.apply(this.subtitle));
    }
}
