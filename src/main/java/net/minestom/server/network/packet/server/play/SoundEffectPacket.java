package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
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
    private static final NetworkBuffer.Type<Point> VECTOR3FI = new Type<>() {
        @Override
        public void write(NetworkBuffer buffer, Point value) {
            buffer.write(INT, (int) value.x());
            buffer.write(INT, (int) value.y());
            buffer.write(INT, (int) value.z());
        }

        @Override
        public Point read(NetworkBuffer buffer) {
            return new Vec(buffer.read(INT), buffer.read(INT), buffer.read(INT));
        }
    };

    public static final NetworkBuffer.Type<SoundEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, SoundEffectPacket::soundEvent,
            AdventurePacketConvertor.SOUND_SOURCE_TYPE, SoundEffectPacket::source,
            VECTOR3FI.transform(point -> point.div(8.0d), point -> point.mul(8.0d)), SoundEffectPacket::origin,
            FLOAT, SoundEffectPacket::volume,
            FLOAT, SoundEffectPacket::pitch,
            LONG, SoundEffectPacket::seed,
            SoundEffectPacket::new
    );

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
