package net.minestom.server.network.packet.server.login;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.JSON_COMPONENT;

public record LoginDisconnectPacket(Component kickMessage) implements ServerPacket.Login,
        ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<LoginDisconnectPacket> SERIALIZER = NetworkBufferTemplate.template(
            JSON_COMPONENT, LoginDisconnectPacket::kickMessage,
            LoginDisconnectPacket::new);

    @Override
    public Collection<Component> components() {
        return List.of(this.kickMessage);
    }

    @Override
    public LoginDisconnectPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new LoginDisconnectPacket(operator.apply(this.kickMessage));
    }
}
