package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record CollectItemPacket(int collectedEntityId, int collectorEntityId, int pickupItemCount)
        implements ServerPacket {
    public CollectItemPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarInt(), reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(collectedEntityId);
        writer.writeVarInt(collectorEntityId);
        writer.writeVarInt(pickupItemCount);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.COLLECT_ITEM;
    }
}
