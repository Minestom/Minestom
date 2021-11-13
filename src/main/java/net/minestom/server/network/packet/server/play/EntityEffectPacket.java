package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.Potion;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EntityEffectPacket(int entityId, Potion potion) implements ServerPacket {
    public EntityEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), new Potion(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) potion.effect().id());
        writer.writeByte(potion.amplifier());
        writer.writeVarInt(potion.duration());
        writer.writeByte(potion.flags());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EFFECT;
    }
}
