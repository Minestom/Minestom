package net.minestom.server.particle.data;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public interface ParticleData {
    void write(@NotNull NetworkBuffer writer);

    static ParticleData read(int particleId, NetworkBuffer reader) {
        if (particleId == Particle.BLOCK.id()) return new BlockParticleData(reader);
        else if (particleId == Particle.BLOCK_MARKER.id()) return new BlockMarkerParticleData(reader);
        else if (particleId == Particle.DUST.id()) return new DustParticleData(reader);
        else if (particleId == Particle.DUST_COLOR_TRANSITION.id()) return new DustColorTransitionParticleData(reader);
        else if (particleId == Particle.FALLING_DUST.id()) return new FallingDustParticleData(reader);
        else if (particleId == Particle.SCULK_CHARGE.id()) return new SculkChargeParticleData(reader);
        else if (particleId == Particle.ITEM.id()) return new ItemParticleData(reader);
        else if (particleId == Particle.VIBRATION.id()) {
            int type = reader.read(NetworkBuffer.VAR_INT);
            if (type == 0) return new VibrationBlockParticleData(reader);
            else if (type == 1) return new VibrationBlockParticleData(reader);
            else return null;
        } else if (particleId == Particle.SHRIEK.id()) return new ShriekParticleData(reader);
        else return null;
    }
}
