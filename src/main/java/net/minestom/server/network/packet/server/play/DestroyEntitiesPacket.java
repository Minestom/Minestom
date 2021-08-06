package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds;

    public DestroyEntitiesPacket(int[] entityIds) {
        this.entityIds = entityIds;
    }

    public DestroyEntitiesPacket(int entityId) {
        this(new int[]{entityId});
    }

    public DestroyEntitiesPacket() {
        this(0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.entityIds = reader.readVarIntArray();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
