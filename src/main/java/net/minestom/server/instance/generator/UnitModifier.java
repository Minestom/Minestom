package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

public interface UnitModifier extends Block.Setter, Biome.Setter {
    void setRelative(int x, int y, int z, @NotNull Block block);

    void setAll(@NotNull Supplier supplier);

    void setAllRelative(@NotNull Supplier supplier);

    void fill(@NotNull Block block);

    void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block);

    void fillHeight(int minHeight, int maxHeight, @NotNull Block block);

    void fillBiome(@NotNull Biome biome);

    interface Supplier {
        @NotNull Block get(int x, int y, int z);
    }
}
