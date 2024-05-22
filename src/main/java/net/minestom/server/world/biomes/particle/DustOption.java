package net.minestom.server.world.biomes.particle;

import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

/**
 * Particle effect which used based on the color values.
 * @param red can between 0 and 1 and is divided by 255
 * @param green can between 0 and 1 and is divided by 255
 * @param blue can between 0 and 1 and is divided by 255
 * @param scale can between 0.01 to 4
 */
@ApiStatus.Experimental
public record DustOption(float red, float green, float blue, float scale) implements BiomeOption {
    @Override
    public @NotNull NBTCompound toNbt() {
        return NBT.Compound(Map.of(
                "type", NBT.String(getType().namespace().toString()),
                "Red", NBT.Float(red),
                "Green", NBT.Float(green),
                "Blue", NBT.Float(blue),
                "Scale", NBT.Float(scale)));
    }

    @Override
    public @NotNull Particle getType() {
        return Particle.DUST;
    }
}
