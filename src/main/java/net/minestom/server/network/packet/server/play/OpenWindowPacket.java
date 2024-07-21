package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record OpenWindowPacket(int windowId, int windowType,
                               @NotNull Component title) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public OpenWindowPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(VAR_INT), reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, windowId);
        writer.write(VAR_INT, windowType);
        writer.write(COMPONENT, title);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.title);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new OpenWindowPacket(this.windowId, this.windowType, operator.apply(this.title));
    }
}
