package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.binary.BinaryWriter;

public class RemoveEntityEffectPacket implements ServerPacket {

    public int entityId;
    public PotionEffect effect;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) effect.getId());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.REMOVE_ENTITY_EFFECT;
    }
}
