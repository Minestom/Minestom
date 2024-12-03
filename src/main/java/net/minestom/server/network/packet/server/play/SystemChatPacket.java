package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.COMPONENT;

public record SystemChatPacket(@NotNull Component message,
                               boolean overlay) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<SystemChatPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, SystemChatPacket::message,
            BOOLEAN, SystemChatPacket::overlay,
            SystemChatPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new SystemChatPacket(operator.apply(message), overlay);
    }
}
