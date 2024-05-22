package net.minestom.server.world.biomes.particle;

import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;

@ApiStatus.Experimental
public record BlockOption(@NotNull Block block) implements BiomeOption {

    @Override
    public @NotNull NBTCompound toNbt() {
        return NBT.Compound(nbtCompound -> {
            nbtCompound.setString("type", getType().namespace().toString());
            nbtCompound.setString("Name", block.name());
            Map<String, String> propertiesMap = block.properties();
            if (!propertiesMap.isEmpty()) {
                nbtCompound.set("Properties", NBT.Compound(p -> propertiesMap.forEach(p::setString)));
            }
        });
    }

    @Override
    public @NotNull Particle getType() {
        return Particle.BLOCK;
    }

}
