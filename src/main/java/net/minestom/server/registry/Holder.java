package net.minestom.server.registry;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * <p>Represents either a reference to a registry entry {@link RegistryKey} or a direct registry value.</p>
 *
 * <p>Whether registry values implement this type depends on client support for direct values.</p>
 *
 * @param <T> the type of the registry entry
 */
public sealed interface Holder<T> permits RegistryKey, Holder.Direct {

    @ApiStatus.NonExtendable
    non-sealed interface Direct<T> extends Holder<T> {
    }

    static <T extends Holder<T>> NetworkBuffer.Type<Holder<T>> networkType(
            Registries.Selector<T> selector,
            NetworkBuffer.Type<T> registryNetworkType
    ) {
        return new RegistryNetworkTypes.HolderNetworkTypeImpl<>(selector, registryNetworkType);
    }

    static <T extends Holder<T>> Codec<Holder<T>> codec(
            Registries.Selector<T> selector,
            Codec<T> registryCodec
    ) {
        return new RegistryCodecs.HolderCodec<>(selector, registryCodec);
    }

    default boolean isDirect() {
        return !(this instanceof RegistryKey<T>);
    }

    default @Nullable RegistryKey<T> asKey() {
        return this instanceof RegistryKey<T> ? (RegistryKey<T>) this : null;
    }

    default @Nullable T asValue() {
        //noinspection unchecked
        return this instanceof RegistryKey<T> ? null : (T) this;
    }

    default Either<RegistryKey<T>, T> unwrap() {
        if (this instanceof RegistryKey<T> key) {
            return Either.left(key);
        } else {
            //noinspection unchecked
            return Either.right((T) this);
        }
    }

    default @Nullable T resolve(DynamicRegistry<T> registry) {
        final var key = asKey();
        if (key != null) {
            return registry.get(key);
        } else {
            //noinspection unchecked
            return (T) this;
        }
    }

}
