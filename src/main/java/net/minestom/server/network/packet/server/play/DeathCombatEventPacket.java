package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public record DeathCombatEventPacket(int playerId, int entityId,
                                     @NotNull Component message) implements ComponentHoldingServerPacket {
    public DeathCombatEventPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readInt(), reader.readComponent());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(playerId);
        writer.writeInt(entityId);
        writer.writeComponent(message);
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
        return new DeathCombatEventPacket(this.playerId, this.entityId, operator.apply(this.message));
    }
}
