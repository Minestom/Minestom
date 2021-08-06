package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class EntityEffectPacket implements ServerPacket {

    public int entityId;
    public Potion potion;

    public EntityEffectPacket() {
        potion = new Potion(PotionEffect.ABSORPTION, (byte) 0, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) potion.getEffect().id());
        writer.writeByte(potion.getAmplifier());
        writer.writeVarInt(potion.getDuration());
        writer.writeByte(potion.getFlags());
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();
        byte potionEffectID = reader.readByte();
        byte amplifier = reader.readByte();
        int duration = reader.readVarInt();
        byte flags = reader.readByte();
        boolean ambient = (flags & 0x01) == 0x01;
        boolean particles = (flags & 0x02) == 0x02;
        boolean icon = (flags & 0x04) == 0x04;

        potion = new Potion(PotionEffect.fromId(potionEffectID), amplifier, duration, particles, icon, ambient);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
