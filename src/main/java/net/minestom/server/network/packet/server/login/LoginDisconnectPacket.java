package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.JSON_COMPONENT;

public record LoginDisconnectPacket(@NotNull Component kickMessage) implements ServerPacket.Login,
        ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<LoginDisconnectPacket> SERIALIZER = NetworkBufferTemplate.template(
            JSON_COMPONENT, LoginDisconnectPacket::kickMessage,
            LoginDisconnectPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.kickMessage);
    }

    @Override
    public @NotNull LoginDisconnectPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new LoginDisconnectPacket(operator.apply(this.kickMessage));
    }
}
