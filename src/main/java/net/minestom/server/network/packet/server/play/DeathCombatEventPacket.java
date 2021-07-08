package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

public class DeathCombatEventPacket implements ComponentHoldingServerPacket {

    public int playerId;
    public int entityId;
    public Component message = Component.empty();

    public static DeathCombatEventPacket of(int playerId, int entityId, Component message) {
        DeathCombatEventPacket packet = new DeathCombatEventPacket();
        packet.playerId = playerId;
        packet.entityId = entityId;
        packet.message = message;
        return packet;
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.playerId = reader.readVarInt();
        this.entityId = reader.readInt();
        this.message = reader.readComponent();
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
        return Collections.singleton(message);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return DeathCombatEventPacket.of(playerId, entityId, operator.apply(message));
    }
}
