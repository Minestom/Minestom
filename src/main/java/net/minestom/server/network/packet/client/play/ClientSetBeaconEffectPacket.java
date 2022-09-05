package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClientSetBeaconEffectPacket(@Nullable PotionType primaryEffect,
                                          @Nullable PotionType secondaryEffect) implements ClientPacket {
    public ClientSetBeaconEffectPacket(BinaryReader reader) {
        this(reader.readBoolean() ? PotionType.fromId(reader.readVarInt()) : null,
                reader.readBoolean() ? PotionType.fromId(reader.readVarInt()) : null);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeBoolean(primaryEffect != null);
        if (primaryEffect != null) writer.writeVarInt(primaryEffect.id());
        writer.writeBoolean(secondaryEffect != null);
        if (secondaryEffect != null) writer.writeVarInt(secondaryEffect.id());
    }
}
