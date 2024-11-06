package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * <p>Holds registry data for any of the registries controlled by the server. Entries in registries should be referenced
 * using a {@link Key} object as opposed to the record type. For example, a biome should be stored as
 * `DynamicRegistry.Key Biome`, as opposed to `Biome` directly.</p>
 *
 * <p>Builtin registries should be accessed via a {@link Registries} instance (currently implemented by
 * {@link net.minestom.server.ServerProcess}, or from {@link net.minestom.server.MinecraftServer} static methods.</p>
 *
 * @param <T> The type of the registry entries
 *
 * @see Registries
 */
public sealed interface DynamicRegistry<T> permits DynamicRegistryImpl {

    /**
     * A key for a {@link ProtocolObject} in a {@link DynamicRegistry}.
     *
     * @param <T> Unused, except to provide compile-time safety and self documentation.
     */
    sealed interface Key<T> extends Keyed permits DynamicRegistryImpl.KeyImpl {

        static <T> @NotNull Key<T> of(@NotNull String namespace) {
            return new DynamicRegistryImpl.KeyImpl<>(NamespaceID.from(namespace));
        }

        static <T> @NotNull Key<T> of(@NotNull NamespaceID namespace) {
            return new DynamicRegistryImpl.KeyImpl<>(namespace);
        }

        @Contract(pure = true)
        @NotNull NamespaceID namespace();

        @Contract(pure = true)
        default @NotNull String name() {
            return namespace().asString();
        }

        @Override
        @Contract(pure = true)
        default @NotNull net.kyori.adventure.key.Key key() {
            return namespace();
        }
    }

    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull String id) {
        return new DynamicRegistryImpl<>(id, null);
    }

    /**
     * Creates a new empty registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(
            @NotNull String id, @NotNull BinaryTagSerializer<T> nbtType) {
        return new DynamicRegistryImpl<>(id, nbtType);
    }

    /**
     * Creates a new empty registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T extends ProtocolObject> @NotNull DynamicRegistry<T> create(
            @NotNull String id, @NotNull BinaryTagSerializer<T> nbtType,
            @NotNull Registry.Resource resource, @NotNull Registry.Container.Loader<T> loader) {
        return create(id, nbtType, resource, loader, null);
    }

    /**
     * Creates a new empty registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T extends ProtocolObject> @NotNull DynamicRegistry<T> create(
            @NotNull String id, @NotNull BinaryTagSerializer<T> nbtType,
            @NotNull Registry.Resource resource, @NotNull Registry.Container.Loader<T> loader,
            @Nullable Comparator<String> idComparator) {
        final DynamicRegistry<T> registry = new DynamicRegistryImpl<>(id, nbtType);
        DynamicRegistryImpl.loadStaticRegistry(registry, resource, loader, idComparator);
        return registry;
    }

    /**
     * Creates a new empty registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T extends ProtocolObject> @NotNull DynamicRegistry<T> create(
            @NotNull String id, @NotNull BinaryTagSerializer<T> nbtType,
            @NotNull Registries registries, @NotNull Registry.Resource resource) {
        final DynamicRegistryImpl<T> registry = new DynamicRegistryImpl<>(id, nbtType);
        DynamicRegistryImpl.loadStaticSnbtRegistry(registries, registry, resource);
        return registry;
    }

    @NotNull String id();

    @Nullable T get(int id);
    @Nullable T get(@NotNull NamespaceID namespace);
    default @Nullable T get(@NotNull Key<T> key) {
        return get(key.namespace());
    }

    @Nullable Key<T> getKey(int id);
    @Nullable Key<T> getKey(@NotNull T value);
    @Nullable NamespaceID getName(int id);
    @Nullable DataPack getPack(int id);
    default @Nullable DataPack getPack(@NotNull Key<T> key) {
        final int id = getId(key);
        return id == -1 ? null : getPack(id);
    }

    /**
     * Returns the protocol ID associated with the given {@link NamespaceID}, or -1 if none is registered.
     *
     * @see #register(NamespaceID, T)
     */
    int getId(@NotNull NamespaceID id);

    /**
     * Returns the protocol ID associated with the given {@link Key}, or -1 if none is registered.
     *
     * @see #register(NamespaceID, T)
     */
    default int getId(@NotNull Key<T> key) {
        return getId(key.namespace());
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

    /**
     * <p>Register an object to this registry, overwriting the previous entry if any is present.</p>
     *
     * <p>Note: the new registry will not be sent to existing players. They must be returned to
     * the configuration phase to receive new registry data. See {@link Player#startConfigurationPhase()}.</p>
     *
     * <p><b>WARNING:</b> Updating an existing entry is an inherently unsafe operation as it may cause desync with
     * existing structures. <b>This behavior is disabled by default, and must be enabled by setting the system
     * property <code>minestom.registry.unsafe-ops</code> to <code>true</code>.</b></p>
     *
     * @param object The entry to register
     * @return The new ID of the registered object
     */
    default @NotNull DynamicRegistry.Key<T> register(@NotNull String id, @NotNull T object) {
        return register(NamespaceID.from(id), object, null);
    }

    default @NotNull DynamicRegistry.Key<T> register(@NotNull NamespaceID id, @NotNull T object) {
        return register(id, object, null);
    }

    @ApiStatus.Internal
    default @NotNull DynamicRegistry.Key<T> register(@NotNull String id, @NotNull T object, @Nullable DataPack pack) {
        return register(NamespaceID.from(id), object, pack);
    }

    @ApiStatus.Internal
    default @NotNull DynamicRegistry.Key<T> register(@NotNull NamespaceID id, @NotNull T object, @Nullable DataPack pack) {
        return register(id, object);
    }

    /**
     * <p>Removes an object from this registry.</p>
     *
     * <p><b>WARNING:</b> This operation will cause all subsequent IDs to be remapped, meaning that any loaded entry
     * with existing IDs may be incorrect. For example, loading a world with 0=plains, 1=desert, 2=badlands would store
     * those IDs in the palette. If you then deleted entry 1 (desert), any desert biomes in the loaded world would
     * become badlands, and any badlands would become invalid. <b>This behavior is disabled by default, and must be
     * enabled by setting the system property <code>minestom.registry.unsafe-ops</code> to <code>true</code>.</b></p>
     *
     * <p>Note: the new registry will not be sent to existing players. They must be returned to
     * the configuration phase to receive new registry data. See {@link Player#startConfigurationPhase()}.</p>
     *
     * @param namespaceId The id of the entry to remove
     * @return True if the object was removed, false if it was not present
     * @throws UnsupportedOperationException If the system property <code>minestom.registry.unsafe-remove</code> is not set to <code>true</code>
     */
    boolean remove(@NotNull NamespaceID namespaceId) throws UnsupportedOperationException;

    /**
     * <p>Returns a {@link SendablePacket} potentially excluding vanilla entries if possible. It is never possible to
     * exclude vanilla entries if one has been overridden (e.g. via {@link #register(NamespaceID, T)}.</p>
     *
     * @param registries Registries provider
     * @param excludeVanilla Whether to exclude vanilla entries
     * @return A {@link SendablePacket} containing the registry data
     */
    @ApiStatus.Internal
    @NotNull SendablePacket registryDataPacket(@NotNull Registries registries, boolean excludeVanilla);

}
