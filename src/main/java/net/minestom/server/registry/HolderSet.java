package net.minestom.server.registry;

import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * A HolderSet is either a registry tag or a list of direct holders. Mixing is not allowed.
 */
public sealed interface HolderSet<T> permits HolderSet.Direct, RegistryTag {

    static <T extends Holder<T>> @NotNull Codec<HolderSet<T>> codec(
            @NotNull Registries.Selector<T> selector,
            @NotNull Codec<T> registryCodec
    ) {
        return new RegistryCodecs.HolderSetImpl<>(RegistryTag.codec(selector), registryCodec);
    }

    record Direct<T extends Holder.Direct<T>>(@NotNull List<T> values) implements HolderSet<T>, Iterable<T> {

        @SafeVarargs
        public Direct(@NotNull T... values) {
            this(List.of(values));
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return values.iterator();
        }

    }

}
