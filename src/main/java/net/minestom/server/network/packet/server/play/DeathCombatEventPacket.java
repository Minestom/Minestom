package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record DeathCombatEventPacket(int playerId, int entityId,
                                     @NotNull Component message) implements ServerPacket {
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
}
