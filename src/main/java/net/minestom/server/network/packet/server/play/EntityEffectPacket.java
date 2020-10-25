package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public PotionEffect effect;
    public byte amplifier;
    public int duration;
    public byte flags;

    @Override
    public void write(@NotNull BinaryWriter writer) {
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
