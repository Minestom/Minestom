package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.WeightedList;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public record ExplosionPacket(
        Point center, float radius, int blockCount,
        @Nullable Point playerKnockback,
        Particle particle, SoundEvent sound,
        WeightedList<BlockParticleInfo> blockParticles
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ExplosionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VECTOR3D, ExplosionPacket::center,
            NetworkBuffer.FLOAT, ExplosionPacket::radius,
            NetworkBuffer.INT, ExplosionPacket::blockCount,
            VECTOR3D.optional(), ExplosionPacket::playerKnockback,
            Particle.NETWORK_TYPE, ExplosionPacket::particle,
            SoundEvent.NETWORK_TYPE, ExplosionPacket::sound,
            WeightedList.networkType(BlockParticleInfo.SERIALIZER), ExplosionPacket::blockParticles,
            ExplosionPacket::new);

    public record BlockParticleInfo(Particle particle, float scaling, float speed) {
        public static final NetworkBuffer.Type<BlockParticleInfo> SERIALIZER = NetworkBufferTemplate.template(
                Particle.NETWORK_TYPE, BlockParticleInfo::particle,
                NetworkBuffer.FLOAT, BlockParticleInfo::scaling,
                NetworkBuffer.FLOAT, BlockParticleInfo::speed,
                BlockParticleInfo::new);
    }
}
