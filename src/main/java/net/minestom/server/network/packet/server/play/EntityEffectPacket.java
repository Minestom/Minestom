package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public Potion potion;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) potion.getEffect().getId());
        writer.writeByte(potion.getAmplifier());
        writer.writeVarInt(potion.getDuration());
        writer.writeByte(potion.getFlags());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
