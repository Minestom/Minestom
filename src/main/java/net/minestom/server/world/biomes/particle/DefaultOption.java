package net.minestom.server.world.biomes.particle;

import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public record DefaultOption(@NotNull Particle particle) implements BiomeOption {
    @Override
    public @NotNull NBTCompound toNbt() {
        return NBT.Compound(Map.of("type", NBT.String(getType().namespace().toString())));
    }

    @Override
    public @NotNull Particle getType() {
        return particle;
    }
}
