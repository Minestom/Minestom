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
    public static NetworkBuffer.Type<ClientSetBeaconEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, ClientSetBeaconEffectPacket value) {
            writer.write(BOOLEAN, value.primaryEffect != null);
            if (value.primaryEffect != null) writer.write(VAR_INT, value.primaryEffect.id());
            writer.write(BOOLEAN, value.secondaryEffect != null);
            if (value.secondaryEffect != null) writer.write(VAR_INT, value.secondaryEffect.id());
        }

        @Override
        public ClientSetBeaconEffectPacket read(@NotNull NetworkBuffer reader) {
            return new ClientSetBeaconEffectPacket(reader.read(BOOLEAN) ? PotionType.fromId(reader.read(VAR_INT)) : null,
                    reader.read(BOOLEAN) ? PotionType.fromId(reader.read(VAR_INT)) : null);
        }
    };
}
