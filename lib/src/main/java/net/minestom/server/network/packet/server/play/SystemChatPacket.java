package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record SystemChatPacket(Component message,
                               boolean overlay) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SystemChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, SystemChatPacket::message,
            BOOLEAN, SystemChatPacket::overlay,
            SystemChatPacket::new);

    @Override
    public Collection<Component> components() {
        return List.of(message);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new SystemChatPacket(operator.apply(message), overlay);
    }
}
