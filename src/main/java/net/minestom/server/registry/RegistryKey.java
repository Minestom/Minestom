package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a reference to a {@link Registry} entry.
 *
 * @param <T> the type of the registry entry
 */
@ApiStatus.NonExtendable
public non-sealed interface RegistryKey<T> extends Holder<T>, Keyed {

    static <T> NetworkBuffer.Type<RegistryKey<T>> networkType(Registries.Selector<T> selector) {
        return new RegistryNetworkTypes.RegistryKeyImpl<>(selector);
    }

    static <T> Codec<RegistryKey<T>> codec(Registries.Selector<T> selector) {
        return new RegistryCodecs.RegistryKeyImpl<>(selector);
    }

    static <T> NetworkBuffer.Type<RegistryKey<T>> uncheckedNetworkType() {
        return NetworkBuffer.KEY.transform(RegistryKeyImpl::new, RegistryKey::key);
    }

    static <T> Codec<RegistryKey<T>> uncheckedCodec() {
        return Codec.KEY.transform(RegistryKeyImpl::new, RegistryKey::key);
    }

    /**
     * Creates a typed key from a string. The key syntax is validated, registry membership is not. A key is only a
     * typed name: the same key can name a registered entry in one registry and nothing in another. Lookups with a
     * key that has no entry return {@code null} or {@code -1}.
     *
     * @param key the entry key
     * @param <T> the registry entry type
     * @return the typed registry key
     * @throws IllegalArgumentException if {@code key} is not valid
     * @throws NullPointerException     if {@code key} is {@code null}
     */
    static <T> RegistryKey<T> of(@KeyPattern String key) {
        return of(Key.key(key));
    }

    /**
     * Creates a typed key without checking registry membership. See {@link #of(String)}.
     *
     * @param key the entry key
     * @param <T> the registry entry type
     * @return the typed registry key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    static <T> RegistryKey<T> of(Key key) {
        return new RegistryKeyImpl<>(key);
    }

    default String name() {
        return key().asString();
    }

}
