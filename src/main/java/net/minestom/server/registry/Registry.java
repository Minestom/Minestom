package net.minestom.server.registry;

import net.minestom.server.gamedata.DataPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Registry<T> permits StaticRegistry, DynamicRegistry {

    @NotNull String id();

    @Nullable T get(int id);
    @Nullable T get(@NotNull net.kyori.adventure.key.Key key);
    default @Nullable T get(@NotNull DynamicRegistry.Key<T> key) {
        return get(key.key());
    }

    @Nullable DynamicRegistry.Key<T> getKey(int id);
    @Nullable DynamicRegistry.Key<T> getKey(@NotNull T value);
    @Nullable net.kyori.adventure.key.Key getName(int id);
    @Nullable DataPack getPack(int id);
    default @Nullable DataPack getPack(@NotNull DynamicRegistry.Key<T> key) {
        final int id = getId(key);
        return id == -1 ? null : getPack(id);
    }

    /**
     * Returns the protocol ID associated with the given {@link net.kyori.adventure.key.Key}, or -1 if none is registered.
     */
    int getId(@NotNull net.kyori.adventure.key.Key id);

    /**
     * Returns the protocol ID associated with the given {@link DynamicRegistry.Key}, or -1 if none is registered.
     */
    default int getId(@NotNull DynamicRegistry.Key<T> key) {
        return getId(key.key());
    }

    /**
     * <p>Returns the entries in this registry as an immutable list. The indices in the returned list correspond
     * to the protocol ID of each entry.</p>
     *
     * <p>Note: The returned list is not guaranteed to update with the registry,
     * it should be fetched again for updated values.</p>
     *
     * @return An immutable list of the entries in this registry.
     */
    @NotNull List<T> values();

}
