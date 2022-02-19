package net.minestom.server.map;

import org.jetbrains.annotations.NotNull;

/**
 * How does Minestom compute RGB to MapColor transitions?
 */
public interface ColorMappingStrategy {
    @NotNull
    PreciseMapColor closestColor(int argb);
}