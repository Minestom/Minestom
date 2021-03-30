package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityMovementPacket implements ServerPacket {

    public int entityId;

    public EntityMovementPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_MOVEMENT;
    }
}
