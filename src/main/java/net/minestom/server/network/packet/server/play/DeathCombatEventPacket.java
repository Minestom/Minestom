package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.COMPONENT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record DeathCombatEventPacket(int playerId,
                                     @NotNull Component message) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<DeathCombatEventPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, DeathCombatEventPacket::playerId,
            COMPONENT, DeathCombatEventPacket::message,
            DeathCombatEventPacket::new);

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new DeathCombatEventPacket(this.playerId, operator.apply(this.message));
    }
}
