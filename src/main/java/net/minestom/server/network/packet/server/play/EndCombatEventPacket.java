package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EndCombatEventPacket(int duration, int entityId) implements ServerPacket {
    public EndCombatEventPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(duration);
        writer.writeInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.END_COMBAT_EVENT;
    }
}
