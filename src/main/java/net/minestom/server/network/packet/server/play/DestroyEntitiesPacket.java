package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DestroyEntitiesPacket(@NotNull List<Integer> entityIds) implements ServerPacket {
    public DestroyEntitiesPacket {
        entityIds = List.copyOf(entityIds);
    }

    public DestroyEntitiesPacket(int entityId) {
        this(List.of(entityId));
    }

    public DestroyEntitiesPacket(BinaryReader reader) {
        this(reader.readVarIntList(BinaryReader::readVarInt));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntList(entityIds, BinaryWriter::writeVarInt);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
