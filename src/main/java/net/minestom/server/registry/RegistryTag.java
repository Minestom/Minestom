package net.minestom.server.registry;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * {@link RegistryTag} is a collection of keys from a particular registry.
 *
 * <p>The collection may be backed by a registry (synced, referenced by {@link TagKey}), or direct (list backed).</p>
 *
 * <p>Note that all elements of a direct tag must still be members of the registry.</p>
 *
 * @param <T> The type of the registry object.
 */
public sealed interface RegistryTag<T> extends HolderSet<T>, Iterable<RegistryKey<T>>
        permits RegistryTagImpl.Empty, RegistryTagImpl.Backed, RegistryTagImpl.Direct {

    static <T> NetworkBuffer.Type<RegistryTag<T>> networkType(Registries.Selector<T> selector) {
        return new RegistryNetworkTypes.RegistryTagImpl<>(selector);
    }

    static <T> Codec<RegistryTag<T>> codec(Registries.Selector<T> selector) {
        return new RegistryCodecs.RegistryTagImpl<>(selector);
    }

    static <T> RegistryTag<T> empty() {
        //noinspection unchecked
        return (RegistryTag<T>) RegistryTagImpl.Empty.INSTANCE;
    }

    @SafeVarargs
    static <T> RegistryTag<T> direct(RegistryKey<T>... keys) {
        if (keys.length == 0) return empty();
        return new RegistryTagImpl.Direct<>(List.of(keys));
    }

    static <T> RegistryTag<T> direct(Collection<RegistryKey<T>> values) {
        if (values.isEmpty()) return empty();
        return new RegistryTagImpl.Direct<>(List.copyOf(values));
    }

    @Nullable TagKey<T> key();

    boolean contains(RegistryKey<T> value);

    int size();

    /**
     * A builder to eventually build a {@link RegistryTag}
     * either backed by a registry with a {@link TagKey}, or direct.
     *
     * @param <T> the type of registry object.
     */
    sealed interface Builder<T> permits RegistryTagImpl.Builder {
        static <T> Builder<T> of() {
            return of(null);
        }

        @ApiStatus.Internal
        static <T> Builder<T> of(@Nullable TagKey<T> key) {
            return new RegistryTagImpl.Builder<>(key);
        }

        void add(RegistryKey<T> value);

        void addAll(Collection<? extends RegistryKey<T>> values);

        RegistryTag<T> build();
    }
}
