package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record PlayerListHeaderAndFooterPacket(@NotNull Component header,
                                              @NotNull Component footer) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public PlayerListHeaderAndFooterPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(COMPONENT), reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(COMPONENT, header);
        writer.write(COMPONENT, footer);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(header, footer);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new PlayerListHeaderAndFooterPacket(operator.apply(header), operator.apply(footer));
    }

}
