package net.minestom.server.particle.data;

import net.minestom.server.color.Color;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public abstract class ParticleData {
    public abstract void write(BinaryWriter writer);

    public @NotNull BlockParticleData block(short blockstateID) {
        return new BlockParticleData(blockstateID);
    }

    public @NotNull DustParticleData dust(@NotNull Color color, float scale) {
        return new DustParticleData(color, scale);
    }

    public @NotNull ItemParticleData item(@NotNull ItemStack item) {
        return new ItemParticleData(item);
    }
}
