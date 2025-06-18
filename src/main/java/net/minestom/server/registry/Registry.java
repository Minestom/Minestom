package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.common.TagsPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface Registry<T> extends Keyed permits StaticRegistry, DynamicRegistry {

    @Nullable T get(int id);
    @Nullable T get(@NotNull Key key);
    default @Nullable T get(@NotNull RegistryKey<T> key) {
        return get(key.key());
    }

    @Nullable RegistryKey<T> getKey(int id);
    @Nullable RegistryKey<T> getKey(@NotNull T value);
    @Nullable RegistryKey<T> getKey(@NotNull Key key);

    /**
     * Returns the protocol ID associated with the given {@link RegistryKey}, or -1 if none is registered.
     */
    int getId(@NotNull RegistryKey<T> key);

    @Nullable DataPack getPack(int id);
    default @Nullable DataPack getPack(@NotNull RegistryKey<T> key) {
        final int id = getId(key);
        return id == -1 ? null : getPack(id);
    }

    /**
     * Returns the number of entries present in this registry.
     */
    int size();

    /**
     * <p>Returns the keys in this registry as an immutable list.</p>
     *
     * <p>Note: The list order is not guaranteed, and the contents are not guaranteed to update with the registry,
     * it should be fetched again for updated values.</p>
     *
     * @return An immutable collection of the keys in this registry.
     */
    @NotNull Collection<RegistryKey<T>> keys();

    /**
     * <p>Returns the entries in this registry as an immutable list.</p>
     *
     * <p>Note: The list order is not guaranteed, and the contents are not guaranteed to update with the registry,
     * it should be fetched again for updated values.</p>
     *
     * @return An immutable list of the entries in this registry.
     */
    @NotNull Collection<T> values();

    // Tags

    /**
     * Get a tag by its key.
     *
     * @param key The key of the tag
     * @return The tag, or null if not found
     */
    @Nullable RegistryTag<T> getTag(@NotNull TagKey<T> key);
    default @Nullable RegistryTag<T> getTag(@NotNull Key key) {
        return getTag(new TagKeyImpl<>(key));
    }
    /**
     * Get a tag by its key, or create it if it does not exist.
     *
     * <p><b>Note that if a tag is created by this operation, it will not be added to clients who previously received tags.
     * You must resend updated registry tags manually for this to take effect. Referencing a tag for which the client
     * is not aware will result in an immediate clientside disconnect.</b></p>
     *
     * @param key The key of the tag
     * @return The tag, never null
     */
    @NotNull RegistryTag<T> getOrCreateTag(@NotNull TagKey<T> key);

    /**
     * Removes the given tag from this registry if it exists.
     *
     * <p>Note that this does _not_ remove the tag from clients who have previously received tags.
     * You must resend updated registry tags manually for this to take effect.</p>
     *
     * @param key The registry tag to remove.
     * @return True if the tag was removed, false if it did not exist in this registry.
     */
    boolean removeTag(@NotNull TagKey<T> key);

    /**
     * <p>Returns the available tags in this registry.</p>
     *
     * <p>Note: The returned list is not guaranteed to update with the registry,
     * it should be fetched again for updated values.</p>
     *
     * @return An immutable collection of the tags in this registry.
     */
    @NotNull Collection<RegistryTag<T>> tags();

    @ApiStatus.Internal
    @NotNull TagsPacket.Registry tagRegistry();

    /**
     * <p>Returns the registry key associated with this registry</p>
     *
     * @return The key associated.
     */
    @NotNull RegistryKey<? extends Registry<T>> registryKey();

    @Override
    default @NotNull Key key() {
        return registryKey().key();
    };
}
