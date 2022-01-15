package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record RemoveEntityEffectPacket(int entityId, @NotNull PotionEffect potionEffect) implements ServerPacket {
    public RemoveEntityEffectPacket(BinaryReader reader) {
        this(reader.readVarInt(), Objects.requireNonNull(PotionEffect.fromId(reader.readByte())));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeByte((byte) potionEffect.id());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.REMOVE_ENTITY_EFFECT;
    }
}
