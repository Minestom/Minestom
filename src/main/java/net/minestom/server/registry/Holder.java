package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A {@link Holder} is either a reference into a registry or a direct value which isnt necessarily registered with the client.
 */
public sealed interface Holder<T> {

    static <T> NetworkBuffer.@NotNull Type<Holder<T>> networkType(@NotNull Registries.Selector<T> selector, @NotNull NetworkBuffer.Type<T> registryNetworkType) {

    }
    static <T> @NotNull Codec<Holder<T>> codec(@NotNull Registries.Selector<T> selector, @NotNull Codec<T> registryCodec) {

    }

    static <T> NetworkBuffer.@NotNull Type<Holder.Lazy<T>> lazyNetworkType(@NotNull Registries.Selector<T> selector, @NotNull NetworkBuffer.Type<T> registryNetworkType) {

    }
    static <T> @NotNull Codec<Holder.Lazy<T>> lazyCodec(@NotNull Registries.Selector<T> selector, @NotNull Codec<T> registryCodec) {

    }

    record Direct<T>(@NotNull T value) implements Holder<T> {
    }

    record Reference<T>(@NotNull DynamicRegistry<T> registry,
                        @NotNull DynamicRegistry.Key<T> key) implements Holder<T> {
    }

    /**
     * A lazy holder allows for the value to be read as a reference without resolving it against the registry.
     * @param <T>
     */
    record Lazy<T>(@UnknownNullability Holder<T> holder, @UnknownNullability Key reference) {

    }
}
