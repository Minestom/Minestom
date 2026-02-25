package net.minestom.server.registry;

import net.minestom.server.codec.Codec;

import java.util.Iterator;
import java.util.List;

/**
 * A HolderSet is either a registry tag or a list of direct holders. Mixing is not allowed.
 */
public sealed interface HolderSet<T> permits HolderSet.Direct, RegistryTag {

    static <T extends Holder<T>> Codec<HolderSet<T>> codec(
            Registries.Selector<T> selector,
            Codec<T> registryCodec
    ) {
        return new RegistryCodecs.HolderSetImpl<>(RegistryTag.codec(selector), registryCodec);
    }

    record Direct<T extends Holder.Direct<T>>(List<T> values) implements HolderSet<T>, Iterable<T> {
        public Direct {
            values = List.copyOf(values);
        }

        @SafeVarargs
        public Direct(T... values) {
            this(List.of(values));
        }

        @Override
        public Iterator<T> iterator() {
            return values.iterator();
        }

    }

}
