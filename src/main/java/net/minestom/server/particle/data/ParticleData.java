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
        else if (particleId == Particle.VIBRATION.id()) return new VibrationParticleData(reader);
        else if (particleId == Particle.SHRIEK.id()) return new ShriekParticleData(reader);
        else return null;
    }

    boolean validate(int particleId);

    static boolean requiresData(int particleId) {
        return particleId == Particle.BLOCK.id()
                || particleId == Particle.BLOCK_MARKER.id()
                || particleId == Particle.DUST.id()
                || particleId == Particle.DUST_COLOR_TRANSITION.id()
                || particleId == Particle.FALLING_DUST.id()
                || particleId == Particle.SCULK_CHARGE.id()
                || particleId == Particle.ITEM.id()
                || particleId == Particle.VIBRATION.id()
                || particleId == Particle.SHRIEK.id();
    }

    static ParticleData defaultData(int id) {
        if (id == Particle.BLOCK.id()) return new BlockParticleData();
        else if (id == Particle.BLOCK_MARKER.id()) return new BlockMarkerParticleData();
        else if (id == Particle.DUST.id()) return new DustParticleData();
        else if (id == Particle.DUST_COLOR_TRANSITION.id()) return new DustColorTransitionParticleData();
        else if (id == Particle.FALLING_DUST.id()) return new FallingDustParticleData();
        else if (id == Particle.SCULK_CHARGE.id()) return new SculkChargeParticleData();
        else if (id == Particle.ITEM.id()) return new ItemParticleData();
        else if (id == Particle.VIBRATION.id()) return new VibrationParticleData();
        else if (id == Particle.SHRIEK.id()) return new ShriekParticleData();
        else return null;
    }
}