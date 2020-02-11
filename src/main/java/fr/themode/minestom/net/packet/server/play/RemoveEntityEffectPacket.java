package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
