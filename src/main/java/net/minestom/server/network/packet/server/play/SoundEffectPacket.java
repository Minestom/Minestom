package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;

import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.LONG;

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
    private static final NetworkBuffer.Type<Integer> INTEGER_TYPE = NetworkBuffer.INT
            .transform(integer -> integer / 8, integer -> integer * 8);

    public static final NetworkBuffer.Type<SoundEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, SoundEffectPacket::soundEvent,
            AdventurePacketConvertor.SOUND_SOURCE_TYPE, SoundEffectPacket::source,
            INTEGER_TYPE, SoundEffectPacket::x,
            INTEGER_TYPE, SoundEffectPacket::y,
            INTEGER_TYPE, SoundEffectPacket::z,
            FLOAT, SoundEffectPacket::volume,
            FLOAT, SoundEffectPacket::pitch,
            LONG, SoundEffectPacket::seed,
            SoundEffectPacket::new
    );

    public SoundEffectPacket(SoundEvent soundEvent, Source source, Point position, float volume, float pitch, long seed) {
        this(soundEvent, source, position.blockX(), position.blockY(), position.blockZ(), volume, pitch, seed);
    }
}
