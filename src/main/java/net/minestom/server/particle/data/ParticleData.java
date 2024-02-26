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

    static ParticleData defaultData(String id) {
        return switch (id) {
            case "minecraft:block" -> new BlockParticleData();
            case "minecraft:block_marker" -> new BlockMarkerParticleData();
            case "minecraft:dust" -> new DustParticleData();
            case "minecraft:dust_color_transition" -> new DustColorTransitionParticleData();
            case "minecraft:falling_dust" -> new FallingDustParticleData();
            case "minecraft:sculk_charge" -> new SculkChargeParticleData();
            case "minecraft:item" -> new ItemParticleData();
            case "minecraft:vibration" -> new VibrationParticleData();
            case "minecraft:shriek" -> new ShriekParticleData();
            default -> null;
        };
    }
}
