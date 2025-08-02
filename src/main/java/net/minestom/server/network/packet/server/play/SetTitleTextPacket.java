package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record SetTitleTextPacket(Component title) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SetTitleTextPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, SetTitleTextPacket::title,
            SetTitleTextPacket::new);

    @Override
    public Collection<Component> components() {
        return List.of(this.title);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new SetTitleTextPacket(operator.apply(this.title));
    }
}
