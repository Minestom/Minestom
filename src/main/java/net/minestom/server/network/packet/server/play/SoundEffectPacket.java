package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;

import static net.minestom.server.network.NetworkBuffer.*;

public record SoundEffectPacket(
        SoundEvent soundEvent,
        Source source,
        Point origin,
        float volume,
        float pitch,
        long seed
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SoundEffectPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, SoundEffectPacket value) {
            buffer.write(SoundEvent.NETWORK_TYPE, value.soundEvent());
            buffer.write(VAR_INT, AdventurePacketConvertor.getSoundSourceValue(value.source()));
            buffer.write(INT, (int)(value.origin.x() * 8));
            buffer.write(INT, (int)(value.origin.y() * 8));
            buffer.write(INT, (int)(value.origin.z() * 8));
            buffer.write(FLOAT, value.volume());
            buffer.write(FLOAT, value.pitch());
            buffer.write(LONG, value.seed());
        }

        @Override
        public SoundEffectPacket read(NetworkBuffer buffer) {
            return new SoundEffectPacket(buffer.read(SoundEvent.NETWORK_TYPE),
                    buffer.read(NetworkBuffer.Enum(Source.class)),
                    new Vec(buffer.read(INT) / 8.0, buffer.read(INT) / 8.0, buffer.read(INT) / 8.0),
                    buffer.read(FLOAT),
                    buffer.read(FLOAT),
                    buffer.read(LONG));
        }
    };

    /**
     * @deprecated Use {@link #SoundEffectPacket(SoundEvent, Source, Point, float, float, long)}
     */
    @Deprecated(forRemoval = true)
    public SoundEffectPacket(SoundEvent soundEvent, Source source, int x, int y, int z, float volume, float pitch, long seed) {
        this(soundEvent, source, new Vec(x, y, z), volume, pitch, seed);
    }

    /**
     * @deprecated Use {@link #origin()} with {@link Point#blockX()} instead.
     */
    @Deprecated(forRemoval = true)
    public int x() {
        return origin.blockX();
    }

    /**
     * @deprecated Use {@link #origin()} with {@link Point#blockY()} instead.
     */
    @Deprecated(forRemoval = true)
    public int y() {
        return origin.blockY();
    }

    /**
     * @deprecated Use {@link #origin()} with {@link Point#blockZ()} instead.
     */
    @Deprecated(forRemoval = true)
    public int z() {
        return origin.blockZ();
    }
}
