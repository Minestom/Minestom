package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public record ExplosionPacket(
        @NotNull Point center, @Nullable Point playerKnockback,
        @NotNull Particle particle, @NotNull SoundEvent sound
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ExplosionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VECTOR3D, ExplosionPacket::center,
            VECTOR3D.optional(), ExplosionPacket::playerKnockback,
            Particle.NETWORK_TYPE, ExplosionPacket::particle,
            SoundEvent.NETWORK_TYPE, ExplosionPacket::sound,
            ExplosionPacket::new);

}
