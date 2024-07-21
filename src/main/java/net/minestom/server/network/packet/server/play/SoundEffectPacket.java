package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record SoundEffectPacket(
        @NotNull SoundEvent soundEvent,
        @NotNull Source source,
        int x,
        int y,
        int z,
        float volume,
        float pitch,
        long seed
) implements ServerPacket.Play {

    public SoundEffectPacket(@NotNull SoundEvent soundEvent, @NotNull Source source, @NotNull Point position, float volume, float pitch, long seed) {
        this(soundEvent, source, position.blockX(), position.blockY(), position.blockZ(), volume, pitch, seed);
    }

    public SoundEffectPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(SoundEvent.NETWORK_TYPE),
                reader.readEnum(Source.class),
                reader.read(INT) * 8,
                reader.read(INT) * 8,
                reader.read(INT) * 8,
                reader.read(FLOAT),
                reader.read(FLOAT),
                reader.read(LONG));
    }


    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(SoundEvent.NETWORK_TYPE, soundEvent);
        writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(source));
        writer.write(INT, x * 8);
        writer.write(INT, y * 8);
        writer.write(INT, z * 8);
        writer.write(FLOAT, volume);
        writer.write(FLOAT, pitch);
        writer.write(LONG, seed);
    }

}
