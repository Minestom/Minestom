package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class RemoveEntityEffectPacket implements ServerPacket {

    public int entityId;
    public PotionEffect effect = PotionEffect.ABSORPTION;

    public RemoveEntityEffectPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) effect.id());
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        effect = PotionEffect.fromId(reader.readByte());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.REMOVE_ENTITY_EFFECT;
    }
}
