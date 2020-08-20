package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public PotionType effect;
    public byte amplifier;
    public int duration;
    public byte flags;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) effect.getId());
        writer.writeByte(amplifier);
        writer.writeVarInt(duration);
        writer.writeByte(flags);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
