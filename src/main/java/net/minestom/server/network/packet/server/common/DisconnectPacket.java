package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record DisconnectPacket(@NotNull Component message) implements ServerPacket.Configuration, ServerPacket.Play,
        ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<DisconnectPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, DisconnectPacket::message, DisconnectPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new DisconnectPacket(operator.apply(message));
    }
}
