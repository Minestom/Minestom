package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntitySoundEffectPacket(
        // only one of soundEvent and soundName may be present
        @Nullable SoundEvent soundEvent,
        @Nullable String soundName,
        @Nullable Float range, // Only allowed with soundName
        @NotNull Sound.Source source,
        int entityId,
        float volume,
        float pitch,
        long seed
) implements ServerPacket {

    public EntitySoundEffectPacket {
        Check.argCondition(soundEvent == null && soundName == null, "soundEvent and soundName cannot both be null");
        Check.argCondition(soundEvent != null && soundName != null, "soundEvent and soundName cannot both be present");
        Check.argCondition(soundName == null && range != null, "range cannot be present if soundName is null");
    }

    public EntitySoundEffectPacket(@NotNull SoundEvent soundEvent, @Nullable Float range, @NotNull Sound.Source source,
                             int entityId, float volume, float pitch, long seed) {
        this(soundEvent, null, range, source, entityId, volume, pitch, seed);
    }

    public EntitySoundEffectPacket(@NotNull String soundName, @Nullable Float range, @NotNull Sound.Source source,
                             int entityId, float volume, float pitch, long seed) {
        this(null, soundName, range, source, entityId, volume, pitch, seed);
    }

    public EntitySoundEffectPacket(@NotNull NetworkBuffer reader) {
        this(fromReader(reader));
    }

    private EntitySoundEffectPacket(@NotNull EntitySoundEffectPacket packet) {
        this(packet.soundEvent, packet.soundName, packet.range, packet.source, packet.entityId, packet.volume, packet.pitch, packet.seed);
    }

    private static @NotNull EntitySoundEffectPacket fromReader(@NotNull NetworkBuffer reader) {
        int soundId = reader.read(VAR_INT);
        SoundEvent soundEvent;
        String soundName;
        Float range = null;
        if (soundId == 0) {
            soundEvent = null;
            soundName = reader.read(STRING);
            range = reader.readOptional(FLOAT);
        } else {
            soundEvent = SoundEvent.fromId(soundId - 1);
            soundName = null;
        }
        return new EntitySoundEffectPacket(
                soundEvent,
                soundName,
                range,
                reader.readEnum(Sound.Source.class),
                reader.read(VAR_INT),
                reader.read(FLOAT),
                reader.read(FLOAT),
                reader.read(LONG)
        );
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        if (soundEvent != null) {
            writer.write(VAR_INT, soundEvent.id() + 1);
        } else {
            writer.write(VAR_INT, 0);
            writer.write(STRING, soundName);
            writer.writeOptional(FLOAT, range);
        }
        writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        writer.write(VAR_INT, entityId);
        writer.write(FLOAT, volume);
        writer.write(FLOAT, pitch);
        writer.write(LONG, seed);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_SOUND_EFFECT;
    }
}
