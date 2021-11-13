package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record DestroyEntitiesPacket(int[] entityIds) implements ServerPacket {
    public DestroyEntitiesPacket(int entityId) {
        this(new int[]{entityId});
    }

    public DestroyEntitiesPacket(BinaryReader reader) {
        this(reader.readVarIntArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
