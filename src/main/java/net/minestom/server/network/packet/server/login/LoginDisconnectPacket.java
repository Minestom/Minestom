package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.JSON_COMPONENT;

public record LoginDisconnectPacket(@NotNull Component kickMessage) implements ServerPacket.Login,
        ServerPacket.ComponentHolding {
    public LoginDisconnectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(JSON_COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(JSON_COMPONENT, kickMessage);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.kickMessage);
    }

    @Override
    public @NotNull LoginDisconnectPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new LoginDisconnectPacket(operator.apply(this.kickMessage));
    }
}
