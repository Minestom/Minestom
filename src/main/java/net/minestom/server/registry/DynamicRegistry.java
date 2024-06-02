package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface DynamicRegistry<T extends ProtocolObject> permits DynamicRegistryImpl {

    /**
     * A key for a {@link ProtocolObject} in a {@link DynamicRegistry}.
     *
     * @param <T> Unused, except to provide compile-time safety and self documentation.
     */
    sealed interface Key<T extends ProtocolObject> extends Keyed permits DynamicRegistryImpl.KeyImpl {

        static <T extends ProtocolObject> @NotNull Key<T> of(@NotNull String namespace) {
            return new DynamicRegistryImpl.KeyImpl<>(NamespaceID.from(namespace));
        }

        static <T extends ProtocolObject> @NotNull Key<T> of(@NotNull NamespaceID namespace) {
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

    @Nullable T get(int id);
    @Nullable T get(@NotNull NamespaceID namespace);
    default @Nullable T get(@NotNull Key<T> key) {
        return get(key.namespace());
    }

    @Nullable Key<T> getKey(int id);
    @Nullable NamespaceID getName(int id);

    int getId(@NotNull NamespaceID id);
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
     * @param object The entry to register
     * @return The new ID of the registered object
     */
    @NotNull DynamicRegistry.Key<T> register(@NotNull T object);

    /**
     * <p>Removes an object from this registry.</p>
     *
     * <p><b>WARNING:</b> This operation will cause all subsequent IDs to be remapped, meaning that any loaded entry
     * with existing IDs may be incorrect. For example, loading a world with 0=plains, 1=desert, 2=badlands would store
     * those IDs in the palette. If you then deleted entry 1 (desert), any desert biomes in the loaded world would
     * become badlands, and any badlands would become invalid. <b>This behavior is disabled by default, and must be
     * enabled by setting the system property <code>minestom.registry.unsafe-remove</code> to <code>true</code>.</b></p>
     *
     * <p>Note: the new registry will not be sent to existing players. They must be returned to
     * the configuration phase to receive new registry data. See {@link Player#startConfigurationPhase()}.</p>
     *
     * @param object The entry to remove
     * @return True if the object was removed, false if it was not present
     * @throws UnsupportedOperationException If the system property <code>minestom.registry.unsafe-remove</code> is not set to <code>true</code>
     */
    boolean remove(@NotNull T object) throws UnsupportedOperationException;

    /**
     * <p>Returns a {@link SendablePacket} potentially excluding vanilla entries if possible. It is never possible to
     * exclude vanilla entries if one has been overridden (e.g. via {@link #register(ProtocolObject)}.</p>
     *
     * @param excludeVanilla Whether to exclude vanilla entries
     * @return A {@link SendablePacket} containing the registry data
     */
    @ApiStatus.Internal
    @NotNull SendablePacket registryDataPacket(boolean excludeVanilla);

}
