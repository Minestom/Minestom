package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds = new int[0];

    public DestroyEntitiesPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityIds = reader.readVarIntArray();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
