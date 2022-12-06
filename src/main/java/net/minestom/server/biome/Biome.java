package net.minestom.server.biome;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Biome extends ProtocolObject, Biomes permits BiomeImpl {

    /**
     * Returns the biome registry.
     *
     * @return the biome registry
     */
    @Contract(pure = true)
    @NotNull Registry.BiomeEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespace();
    }

    @Override
    default int id() {
        return registry().id();
    }

    static @NotNull Collection<@NotNull Biome> values() {
        return BiomeImpl.values();
    }

    static @Nullable Biome fromNamespaceId(@NotNull String namespaceID) {
        return BiomeImpl.getSafe(namespaceID);
    }

    static @Nullable Biome fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable Biome fromId(int id) {
        return BiomeImpl.getId(id);
    }
}
