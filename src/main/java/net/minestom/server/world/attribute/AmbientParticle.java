package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.particle.Particle;

public record AmbientParticle(Particle particle, float probability) {
    public static final Codec<AmbientParticle> CODEC = StructCodec.struct(
            "particle", Particle.CODEC, AmbientParticle::particle,
            "probability", Codec.FLOAT, AmbientParticle::probability,
            AmbientParticle::new);
}
