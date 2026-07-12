package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.biome.Biome;

public record Section(Palette blockPalette, Palette biomePalette, Light skyLight, Light blockLight) {
    // Biome ids are no longer guaranteed to place plains at 0 (biomes are now sent in vanilla
    // alphabetical order, see Biome#createDefaultRegistry), so the default "empty" biome is resolved
    // from the registry once it is available and cached. Falls back to 0 during early init / tests.
    private static volatile int cachedDefaultBiomeId = -1;

    public Section(Palette blockPalette, Palette biomePalette) {
        this(blockPalette, biomePalette, Light.sky(), Light.block());
    }

    public Section() {
        this(Palette.blocks(), Palette.biomes());
        this.biomePalette.fill(defaultBiomeId());
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(defaultBiomeId());
    }

    private static int defaultBiomeId() {
        int cached = cachedDefaultBiomeId;
        if (cached >= 0) return cached;
        try {
            final DynamicRegistry<Biome> registry = MinecraftServer.getBiomeRegistry();
            if (registry == null) return 0;
            final int id = registry.getId(Biome.PLAINS);
            if (id < 0) return 0;
            cachedDefaultBiomeId = id;
            return id;
        } catch (Exception ignored) {
            return 0; // registry not ready yet; retry on next call
        }
    }

    public void invalidate() {
        this.skyLight.invalidate();
        this.blockLight.invalidate();
    }

    @Override
    public Section clone() {
        final Light skyLight = Light.sky();
        final Light blockLight = Light.block();

        skyLight.set(this.skyLight.array());
        blockLight.set(this.blockLight.array());

        return new Section(this.blockPalette.clone(), this.biomePalette.clone(), skyLight, blockLight);
    }

    public void setSkyLight(byte[] copyArray) {
        this.skyLight.set(copyArray);
    }

    public void setBlockLight(byte[] copyArray) {
        this.blockLight.set(copyArray);
    }
}
