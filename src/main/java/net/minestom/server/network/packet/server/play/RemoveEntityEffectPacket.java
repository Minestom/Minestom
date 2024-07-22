package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record RemoveEntityEffectPacket(int entityId, @NotNull PotionEffect potionEffect) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<RemoveEntityEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, RemoveEntityEffectPacket value) {
            writer.write(VAR_INT, value.entityId);
            writer.write(VAR_INT, value.potionEffect.id());
        }

        @Override
        public RemoveEntityEffectPacket read(@NotNull NetworkBuffer reader) {
            return new RemoveEntityEffectPacket(reader.read(VAR_INT), Objects.requireNonNull(PotionEffect.fromId(reader.read(VAR_INT))));
        }
    };
}
