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
    public static NetworkBuffer.Type<SoundEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, SoundEffectPacket value) {
            writer.write(SoundEvent.NETWORK_TYPE, value.soundEvent());
            writer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source()));
            writer.write(INT, value.x() * 8);
            writer.write(INT, value.y() * 8);
            writer.write(INT, value.z() * 8);
            writer.write(FLOAT, value.volume());
            writer.write(FLOAT, value.pitch());
            writer.write(LONG, value.seed());
        }

        @Override
        public SoundEffectPacket read(@NotNull NetworkBuffer reader) {
            return new SoundEffectPacket(reader.read(SoundEvent.NETWORK_TYPE),
                    reader.readEnum(Source.class),
                    reader.read(INT) * 8,
                    reader.read(INT) * 8,
                    reader.read(INT) * 8,
                    reader.read(FLOAT),
                    reader.read(FLOAT),
                    reader.read(LONG));
        }
    };

    public SoundEffectPacket(@NotNull SoundEvent soundEvent, @NotNull Source source, @NotNull Point position, float volume, float pitch, long seed) {
        this(soundEvent, source, position.blockX(), position.blockY(), position.blockZ(), volume, pitch, seed);
    }
}
