package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
