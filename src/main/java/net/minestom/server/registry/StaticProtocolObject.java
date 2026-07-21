package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a value from a static protocol registry.
 *
 * <p><strong>Warning:</strong> This interface will no longer extend {@link RegistryKey} in a future release.
 * Use {@link #registryKey()} when a registry key is required.</p>
 *
 * @param <T> the registry value type
 */
@ApiStatus.NonExtendable
public interface StaticProtocolObject<T> extends RegistryKey<T> {

    @Override
    @Contract(pure = true)
    default String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    Key key();

    /**
     * Returns the typed registry key for this value.
     *
     * @return the registry key
     */
    @Contract(pure = true)
    default RegistryKey<T> registryKey() {
        return this;
    }

    @Contract(pure = true)
    int id();

    /**
     * Returns the legacy registry data backing this object.
     *
     * @return the legacy registry data, or {@code null} when none is exposed
     * @deprecated registry values are exposed directly by each protocol object
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    default @Nullable Object registry() {
        return null;
    }
}
