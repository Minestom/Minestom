package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.sound.SoundEvent;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntitySoundEffectPacket(
        SoundEvent soundEvent,
        Sound.Source source,
        int entityId,
        float volume,
        float pitch,
        long seed
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntitySoundEffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            SoundEvent.NETWORK_TYPE, EntitySoundEffectPacket::soundEvent,
            NetworkBuffer.Enum(Sound.Source.class), EntitySoundEffectPacket::source,
            VAR_INT, EntitySoundEffectPacket::entityId,
            FLOAT, EntitySoundEffectPacket::volume,
            FLOAT, EntitySoundEffectPacket::pitch,
            LONG, EntitySoundEffectPacket::seed,
            EntitySoundEffectPacket::new
    );
}
