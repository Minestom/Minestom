package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class RemoveEntityEffectPacket implements ServerPacket {

    public int entityId;
    public byte effectId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte(effectId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.REMOVE_ENTITY_EFFECT;
    }
}
