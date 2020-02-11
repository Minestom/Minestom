package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarIntArray(entityIds);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DESTROY_ENTITIES;
    }
}
