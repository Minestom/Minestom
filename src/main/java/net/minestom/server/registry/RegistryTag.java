package net.minestom.server.registry;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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

    static <T> NetworkBuffer.@NotNull Type<RegistryTag<T>> networkType(@NotNull Registries.Selector<T> selector) {
        return new RegistryNetworkTypes.RegistryTagImpl<>(selector);
    }

    static <T> @NotNull Codec<RegistryTag<T>> codec(@NotNull Registries.Selector<T> selector) {
        return new RegistryCodecs.RegistryTagImpl<>(selector);
    }

    static <T> @NotNull RegistryTag<T> empty() {
        //noinspection unchecked
        return (RegistryTag<T>) RegistryTagImpl.Empty.INSTANCE;
    }

    @SafeVarargs
    static <T> @NotNull RegistryTag<T> direct(@NotNull RegistryKey<T>... keys) {
        if (keys.length == 0) return empty();
        return new RegistryTagImpl.Direct<>(List.of(keys));
    }

    static <T> @NotNull RegistryTag<T> direct(@NotNull Collection<RegistryKey<T>> values) {
        if (values.isEmpty()) return empty();
        return new RegistryTagImpl.Direct<>(List.copyOf(values));
    }

    static <T> @NotNull RegistryTag<T> builder(@Nullable TagKey<T> key, @NotNull Consumer<Builder<T>> consumer) {
        RegistryTagImpl.BuilderImpl<T> builder = new RegistryTagImpl.BuilderImpl<>(key);
        consumer.accept(builder);
        return builder.build();
    }

    @Nullable TagKey<T> key();

    boolean contains(@NotNull RegistryKey<T> value);

    int size();

    sealed interface Builder<T> permits RegistryTagImpl.Backed, RegistryTagImpl.BuilderImpl {

        /**
         * Adds a key to the tag.
         *
         * @param key the key to add
         * @return true if the key was added, false if it was already present
         */
        boolean add(@NotNull RegistryKey<T> key);

        /**
         * Removes a key from the tag.
         *
         * @param key the key to remove
         * @return true if the key was removed, false if it was not present
         */
        boolean remove(@NotNull RegistryKey<T> key);
    }
}
