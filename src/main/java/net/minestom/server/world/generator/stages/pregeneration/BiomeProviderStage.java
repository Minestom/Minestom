package net.minestom.server.world.generator.stages.pregeneration;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.GenerationContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class BiomeProviderStage implements PreGenerationStage<BiomeProviderStage.Data> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeProviderStage.class);
    private final Set<Biome> biomes;

    public BiomeProviderStage(Set<Biome> biomes) {
        this(biomes, false);
    }

    public BiomeProviderStage(Set<Biome> biomes, boolean autoRegister) {
        this.biomes = Collections.unmodifiableSet(biomes);
        for (Biome biome : biomes) {
            if (!MinecraftServer.getBiomeManager().unmodifiableCollection().contains(biome)) {
                if (autoRegister) {
                    MinecraftServer.getBiomeManager().addBiome(biome);
                } else {
                    LOGGER.warn("Biome {} isn't registered in the BiomeManager, chunks containing this biome will be rejected by the client!", biome.name());
                }
            }
        }
    }

    @Override
    public void process(GenerationContext context, int sectionX, int sectionY, int sectionZ) {
        context.setInstanceData(new Data(biomes));
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public int getUniqueId() {
        return INTERNAL_STAGE_ID_OFFSET;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public Function<BinaryReader, Data> getDataReader() {
        return null;
    }

    @Override
    public @NotNull Class<Data> getDataClass() {
        return Data.class;
    }

    public record Data(Set<Biome> biomes) implements StageData.Instance {}
}
