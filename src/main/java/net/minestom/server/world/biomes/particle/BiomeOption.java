package net.minestom.server.world.biomes.particle;

import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Just a generic interface to hold data about biome particles
 * @author TheMeinerLP
 */
public interface BiomeOption {
    @NotNull
    NBTCompound toNbt();
    @NotNull
    Particle getType();
}
