package net.minestom.server.world.biomes;

import net.minestom.server.world.biomes.particle.BiomeOption;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

public record BiomeParticle(float probability, BiomeOption option) {
    public NBTCompound toNbt() {
        return NBT.Compound(Map.of(
                "probability", NBT.Float(probability),
                "options", option.toNbt()));
    }
}
