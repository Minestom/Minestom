package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientSetBeaconEffectPacket(@Nullable PotionType primaryEffect,
                                          @Nullable PotionType secondaryEffect) implements ClientPacket {
    public ClientSetBeaconEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BOOLEAN) ? PotionType.fromId(reader.read(VAR_INT)) : null,
                reader.read(BOOLEAN) ? PotionType.fromId(reader.read(VAR_INT)) : null);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, primaryEffect != null);
        if (primaryEffect != null) writer.write(VAR_INT, primaryEffect.id());
        writer.write(BOOLEAN, secondaryEffect != null);
        if (secondaryEffect != null) writer.write(VAR_INT, secondaryEffect.id());
    }
}
