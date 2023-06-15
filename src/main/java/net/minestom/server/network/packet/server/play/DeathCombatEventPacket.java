package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record DeathCombatEventPacket(int playerId, @NotNull Component message) implements ComponentHoldingServerPacket {
    public DeathCombatEventPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(COMPONENT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, playerId);
        writer.write(COMPONENT, message);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DEATH_COMBAT_EVENT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return List.of(this.message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new DeathCombatEventPacket(this.playerId, operator.apply(this.message));
    }
}
