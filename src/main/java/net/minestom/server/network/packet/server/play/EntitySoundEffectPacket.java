package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntitySoundEffectPacket(
        @NotNull SoundEvent soundEvent,
        @NotNull Sound.Source source,
        int entityId,
        float volume,
        float pitch,
        long seed
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntitySoundEffectPacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntitySoundEffectPacket value) {
            buffer.write(SoundEvent.NETWORK_TYPE, value.soundEvent);
            buffer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source));
            buffer.write(VAR_INT, value.entityId);
            buffer.write(FLOAT, value.volume);
            buffer.write(FLOAT, value.pitch);
            buffer.write(LONG, value.seed);
        }

        @Override
        public EntitySoundEffectPacket read(@NotNull NetworkBuffer buffer) {
            return new EntitySoundEffectPacket(buffer.read(SoundEvent.NETWORK_TYPE),
                    buffer.readEnum(Sound.Source.class),
                    buffer.read(VAR_INT),
                    buffer.read(FLOAT),
                    buffer.read(FLOAT),
                    buffer.read(LONG));
        }
    };
}
