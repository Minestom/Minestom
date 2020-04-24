package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public byte effectId;
    public byte amplifier;
    public int duration;
    public byte flags;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte(effectId);
        writer.writeByte(amplifier);
        writer.writeVarInt(duration);
        writer.writeByte(flags);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
