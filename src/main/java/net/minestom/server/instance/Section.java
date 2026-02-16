package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public sealed interface Section extends Cloneable permits SectionImpl {
    static Section section(Palette blockPalette, Palette biomePalette, Light skyLight, Light blockLight) {
        return new SectionImpl(
                blockPalette, biomePalette,
                skyLight, blockLight,
                new Int2ObjectOpenHashMap<>(), new Int2ObjectOpenHashMap<>(),
                new AtomicLong(1)
        );
    }

    static Section section(Palette blockPalette, Palette biomePalette) {
        return section(blockPalette, biomePalette, Light.sky(), Light.block());
    }

    static Section section() {
        return section(Palette.blocks(), Palette.biomes());
    }

    Palette blockPalette();

    Palette biomePalette();

    void clear();

    Light skyLight();

    Light blockLight();

    Map<Integer, @Nullable Block> entries();

    @Nullable Block cacheBlock(int x, int y, int z, Block block);

    void invalidate();

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    default Section clone() {
        SectionImpl impl = (SectionImpl) this;
        final Light skyLight = Light.sky();
        final Light blockLight = Light.block();
        skyLight.set(impl.skyLight().array());
        blockLight.set(impl.blockLight().array());
        return new SectionImpl(
                impl.blockPalette().clone(), impl.biomePalette().clone(),
                skyLight, blockLight,
                new Int2ObjectOpenHashMap<>(impl.entries()),
                new Int2ObjectOpenHashMap<>(impl.tickableMap()),
                new AtomicLong()
        );
    }
}
