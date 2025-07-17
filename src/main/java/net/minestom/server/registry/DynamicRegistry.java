package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.packet.server.SendablePacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Holds registry data for any of the registries controlled by the server. Entries in registries should be referenced
 * using a {@link RegistryKey} object as opposed to the record type. For example, a biome should be stored as
 * `RegistryKey Biome`, as opposed to `Biome` directly.</p>
 *
 * <p>Builtin registries should be accessed via a {@link Registries} instance (currently implemented by
 * {@link net.minestom.server.ServerProcess}, or from {@link net.minestom.server.MinecraftServer} static methods.</p>
 *
 * @param <T> The type of the registry entries
 * @see Registries
 */
public sealed interface DynamicRegistry<T> extends Registry<T> permits DynamicRegistryImpl {

    @SafeVarargs
    static <T> @NotNull DynamicRegistry<T> fromMap(@NotNull Key key, @NotNull Map.Entry<Key, T>... entries) {
        var registry = new DynamicRegistryImpl<T>(key, null);
        for (var entry : entries)
            registry.register(entry.getKey(), entry.getValue(), null);
        return registry.compact();
    }

    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull Key key) {
        return new DynamicRegistryImpl<>(key, null);
    }

    /**
     * Creates a new empty registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull Key key, @NotNull Codec<T> codec) {
        return new DynamicRegistryImpl<>(key, codec);
    }

    /**
     * Creates a new registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull Key key, @NotNull Codec<T> codec, @NotNull RegistryData.Resource resource) {
        return create(key, codec, null, resource, null, null);
    }

    /**
     * Creates a new registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull Key key, @NotNull Codec<T> codec, @Nullable Registries registries, @NotNull RegistryData.Resource resource) {
        return create(key, codec, registries, resource, null, null);
    }

    /**
     * Creates a new registry of the given type. Should only be used internally.
     *
     * @see Registries
     */
    @ApiStatus.Internal
    static <T> @NotNull DynamicRegistry<T> create(@NotNull Key key, @NotNull Codec<T> codec, @Nullable Registries registries, @NotNull RegistryData.Resource resource, @Nullable Comparator<String> idComparator, @Nullable Codec<T> readCodec) {
        final DynamicRegistryImpl<T> registry = new DynamicRegistryImpl<>(key, codec);
        DynamicRegistryImpl.loadStaticJsonRegistry(registries, registry, resource, idComparator, Objects.requireNonNullElse(readCodec, codec));
        return registry.compact();
    }

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Enchantment> createForEnchantmentsWithSelfReferentialLoadingNightmare(
            @NotNull Key key, @NotNull Codec<Enchantment> codec,
            @NotNull RegistryData.Resource resource, @NotNull Registries registries
    ) {
        final DynamicRegistryImpl<Enchantment> registry = new DynamicRegistryImpl<>(key, codec);
        DynamicRegistryImpl.loadStaticJsonRegistry(new Registries.Delegating(registries) {
            @Override
            public @NotNull DynamicRegistry<Enchantment> enchantment() {
                return registry;
            }
        }, registry, resource, null, codec);
        return registry.compact();
    }

    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Dialog> createForDialogWithSelfReferentialLoadingNightmare(
            @NotNull Key key, @NotNull Codec<Dialog> codec,
            @NotNull RegistryData.Resource resource, @NotNull Registries registries
    ) {
        final DynamicRegistryImpl<Dialog> registry = new DynamicRegistryImpl<>(key, codec);
        DynamicRegistryImpl.loadStaticJsonRegistry(new Registries.Delegating(registries) {
            @Override
            public @NotNull DynamicRegistry<Dialog> dialog() {
                return registry;
            }
        }, registry, resource, null, codec);
        return registry;
    }

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
    default @NotNull RegistryKey<T> register(@NotNull String id, @NotNull T object) {
        return register(Key.key(id), object, null);
    }

    default @NotNull RegistryKey<T> register(@NotNull Key id, @NotNull T object) {
        return register(id, object, null);
    }

    @ApiStatus.Internal
    default @NotNull RegistryKey<T> register(@NotNull String id, @NotNull T object, @Nullable DataPack pack) {
        return register(Key.key(id), object, pack);
    }

    @ApiStatus.Internal
    @NotNull RegistryKey<T> register(@NotNull Key id, @NotNull T object, @Nullable DataPack pack);

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
     * @param key The id of the entry to remove
     * @return True if the object was removed, false if it was not present
     * @throws UnsupportedOperationException If the system property <code>minestom.registry.unsafe-remove</code> is not set to <code>true</code>
     */
    boolean remove(@NotNull Key key) throws UnsupportedOperationException;

    /**
     * <p>Returns a {@link SendablePacket} potentially excluding vanilla entries if possible. It is never possible to
     * exclude vanilla entries if one has been overridden (e.g. via {@link #register(Key, T)}.</p>
     *
     * @param registries     Registries provider
     * @param excludeVanilla Whether to exclude vanilla entries
     * @return A {@link SendablePacket} containing the registry data
     */
    @ApiStatus.Internal
    @NotNull SendablePacket registryDataPacket(@NotNull Registries registries, boolean excludeVanilla);

}
