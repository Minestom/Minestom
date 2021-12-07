package net.minestom.server.snapshot;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Objects;

public non-sealed interface ChunkSnapshot extends Snapshot, Block.Getter, Biome.Getter, TagReadable {
    int chunkX();

    int chunkZ();

    @Override
    default @UnknownNullability Block getBlock(int x, int y, int z, @NotNull Condition condition) {
        SectionSnapshot section = section(y);
        return Objects.requireNonNull(section).getBlock(x, y, z, condition);
    }

    @Override
    default @NotNull Biome getBiome(int x, int y, int z) {
        SectionSnapshot section = section(y);
        return Objects.requireNonNull(section).getBiome(x, y, z);
    }

    @Nullable SectionSnapshot section(int y);

    @NotNull List<@NotNull SectionSnapshot> sections();

    @NotNull List<@NotNull EntitySnapshot> entities();

    @NotNull List<@NotNull PlayerSnapshot> players();
}
