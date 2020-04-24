package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class CollectItemPacket implements ServerPacket {

    public int collectedEntityId;
    public int collectorEntityId;
    public int pickupItemCount;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(collectedEntityId);
        writer.writeVarInt(collectorEntityId);
        writer.writeVarInt(pickupItemCount);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.COLLECT_ITEM;
    }
}
