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
        public void write(@NotNull NetworkBuffer writer, EntitySoundEffectPacket value) {
            writer.write(SoundEvent.NETWORK_TYPE, value.soundEvent);
            writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source));
            writer.write(VAR_INT, value.entityId);
            writer.write(FLOAT, value.volume);
            writer.write(FLOAT, value.pitch);
            writer.write(LONG, value.seed);
        }

        @Override
        public EntitySoundEffectPacket read(@NotNull NetworkBuffer reader) {
            return new EntitySoundEffectPacket(reader.read(SoundEvent.NETWORK_TYPE),
                    reader.readEnum(Sound.Source.class),
                    reader.read(VAR_INT),
                    reader.read(FLOAT),
                    reader.read(FLOAT),
                    reader.read(LONG));
        }
    };
}
