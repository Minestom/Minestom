package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
        return 0x55;
    }
}
