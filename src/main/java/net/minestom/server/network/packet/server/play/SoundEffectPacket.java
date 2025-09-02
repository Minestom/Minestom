package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;

import static net.minestom.server.network.NetworkBuffer.*;

public record SoundEffectPacket(
        SoundEvent soundEvent,
        Source source,
        int x,
        int y,
        int z,
        float volume,
        float pitch,
        long seed
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SoundEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, SoundEffectPacket value) {
            buffer.write(SoundEvent.NETWORK_TYPE, value.soundEvent());
            buffer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source()));
            buffer.write(INT, value.x() * 8);
            buffer.write(INT, value.y() * 8);
            buffer.write(INT, value.z() * 8);
            buffer.write(FLOAT, value.volume());
            buffer.write(FLOAT, value.pitch());
            buffer.write(LONG, value.seed());
        }

        @Override
        public SoundEffectPacket read(NetworkBuffer buffer) {
            return new SoundEffectPacket(buffer.read(SoundEvent.NETWORK_TYPE),
                    buffer.read(NetworkBuffer.Enum(Source.class)),
                    buffer.read(INT) * 8,
                    buffer.read(INT) * 8,
                    buffer.read(INT) * 8,
                    buffer.read(FLOAT),
                    buffer.read(FLOAT),
                    buffer.read(LONG));
        }
    };

    public SoundEffectPacket(SoundEvent soundEvent, Source source, Point position, float volume, float pitch, long seed) {
        this(soundEvent, source, position.blockX(), position.blockY(), position.blockZ(), volume, pitch, seed);
    }
}
