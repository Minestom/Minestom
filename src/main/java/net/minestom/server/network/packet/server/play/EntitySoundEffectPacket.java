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

    public EntitySoundEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(SoundEvent.NETWORK_TYPE),
                reader.readEnum(Sound.Source.class),
                reader.read(VAR_INT),
                reader.read(FLOAT),
                reader.read(FLOAT),
                reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(SoundEvent.NETWORK_TYPE, soundEvent);
        writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        writer.write(VAR_INT, entityId);
        writer.write(FLOAT, volume);
        writer.write(FLOAT, pitch);
        writer.write(LONG, seed);
    }

}
