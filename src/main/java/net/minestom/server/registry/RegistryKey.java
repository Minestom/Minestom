package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
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
     * Creates a new {@link RegistryKey} from the given raw string. Should not be used externally.
     * Registry keys are returned from {@link DynamicRegistry#register(Key, Object)}.
     */
    @ApiStatus.Internal
    static <T> RegistryKey<T> unsafeOf(String key) {
        return unsafeOf(Key.key(key));
    }

    /**
     * Creates a new {@link RegistryKey} from the given raw string. Should not be used externally.
     * Registry keys are returned from {@link DynamicRegistry#register(Key, Object)}.
     */
    @ApiStatus.Internal
    static <T> RegistryKey<T> unsafeOf(Key key) {
        return new RegistryKeyImpl<>(key);
    }

    default String name() {
        return key().asString();
    }

}
