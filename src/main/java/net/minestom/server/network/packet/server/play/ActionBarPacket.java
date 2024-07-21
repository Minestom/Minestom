package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record ActionBarPacket(@NotNull Component text) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public ActionBarPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(COMPONENT, text);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.text);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new ActionBarPacket(operator.apply(this.text));
    }
}
