package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

import static net.minestom.server.coordinate.CoordConversion.sectionBlockIndex;

@NotNullByDefault
record SectionImpl(
        Palette blockPalette, Palette biomePalette,
        Light skyLight, Light blockLight,
        // Key = CoordConversion#sectionBlockIndex
        Int2ObjectOpenHashMap<Block> entries,
        Int2ObjectOpenHashMap<Block> tickableMap,
        AtomicLong version
) implements Section {
    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.entries.clear();
        this.tickableMap.clear();
    }

    @Override
    public @Nullable Block cacheBlock(int x, int y, int z, Block block) {
        final int index = sectionBlockIndex(x, y, z);
        // Handler
        final BlockHandler handler = block.handler();
        final Block lastCachedBlock;
        if (handler != null || block.hasNbt() || block.registry().isBlockEntity()) {
            lastCachedBlock = entries().put(index, block);
        } else {
            lastCachedBlock = entries().remove(index);
        }
        // Block tick
        if (handler != null && handler.isTickable()) {
            tickableMap().put(index, block);
        } else {
            tickableMap().remove(index);
        }
        return lastCachedBlock;
    }

    @Override
    public void invalidate() {
        this.version.incrementAndGet();
        this.skyLight.invalidate();
        this.blockLight.invalidate();
    }

    @Override
    public Section clone() {
        return Section.super.clone();
    }
}
