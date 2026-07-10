package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface Light {
    static Light sky() {
        return new SkyLight();
    }

    static Light block() {
        return new BlockLight();
    }

    boolean requiresSend();

    @ApiStatus.Internal
    byte[] array();

    void flip();

    int getLevel(int x, int y, int z);

    void invalidate();

    boolean requiresUpdate();

    void set(byte[] copyArray);

    @ApiStatus.Internal
    Set<Point> calculateInternal(Palette blockPalette,
                                 int chunkX, int chunkY, int chunkZ,
                                 int[] heightmap, int maxY,
                                 LightLookup lightLookup);

    @ApiStatus.Internal
    Set<Point> calculateExternal(Palette blockPalette,
                                 Point[] neighbors,
                                 LightLookup lightLookup,
                                 PaletteLookup paletteLookup);

    @FunctionalInterface
    interface LightLookup {
        @Nullable Light light(int x, int y, int z);
    }

    @FunctionalInterface
    interface PaletteLookup {
        @Nullable Palette palette(int x, int y, int z);
    }
}
