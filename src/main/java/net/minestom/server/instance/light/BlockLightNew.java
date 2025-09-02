package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

public class BlockLightNew extends AbstractLight {
    @Override
    protected @NotNull InternalCalculation internalCalculation(@NotNull Palette blockPalette, int chunkX, int chunkY, int chunkZ, int[] heightmap, int maxY, @NotNull LightLookup lightLookup) {
        return null;
    }

    @Override
    protected @NotNull ExternalCalculation externalCalculation(@NotNull Palette blockPalette, @NotNull Point @NotNull [] neighbors, @NotNull LightLookup lightLookup, @NotNull PaletteLookup paletteLookup) {
        return null;
    }

    @Override
    public boolean applyInternalCalculation(@NotNull LightCalculation lightCalculation) {
        return false;
    }

    @Override
    public boolean applyExternalCalculation(@NotNull LightCalculation lightCalculation) {
        return false;
    }
}
