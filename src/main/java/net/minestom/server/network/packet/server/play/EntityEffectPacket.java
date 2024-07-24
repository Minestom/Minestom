package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityEffectPacket(int entityId, @NotNull Potion potion) implements ServerPacket.Play {
    public static NetworkBuffer.Type<EntityEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityEffectPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(value.potion);
        }

        @Override
        public EntityEffectPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityEffectPacket(buffer.read(VAR_INT), new Potion(buffer));
        }
    };
}
