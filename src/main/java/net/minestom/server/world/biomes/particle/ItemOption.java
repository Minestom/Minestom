package net.minestom.server.world.biomes.particle;

import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Particle effect which used based on an item inside the biome.
 * @param item to bes used
 */
@ApiStatus.Experimental
public record ItemOption(@NotNull ItemStack item) implements BiomeOption {

    @Override
    public @NotNull NBTCompound toNbt() {
        NBTCompound nbtCompound = item.meta().toNBT();
        return nbtCompound.modify(n -> n.setString("type", getType().namespace().toString()));
    }

    @Override
    public @NotNull Particle getType() {
        return Particle.ITEM;
    }

}
