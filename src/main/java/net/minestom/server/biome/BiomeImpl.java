package net.minestom.server.biome;

import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record BiomeImpl(Registry.BiomeEntry registry) implements Biome {

    private static final Registry.Container<Biome> CONTAINER = Registry.createContainer(Registry.Resource.BIOMES,
            (namespace, properties) -> new BiomeImpl(Registry.biome(namespace, properties)));

    static Biome get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Biome getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Biome getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Biome> values() {
        return CONTAINER.values();
    }

    @Override
    public String toString() {
        return name();
    }

}
