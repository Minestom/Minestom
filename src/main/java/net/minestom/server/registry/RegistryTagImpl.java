package net.minestom.server.registry;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class RegistryTagImpl {

    // Lib tag-invalidation hook. Defaults to a no-op; the framework installs a callback that
    // invalidates the connection tag cache when a backed tag mutates.
    private static volatile Runnable invalidationHook = () -> {
    };

    static void invalidationHook(Runnable hook) {
        invalidationHook = hook;
    }

    record Empty() implements RegistryTag<Object> {
        public static final Empty INSTANCE = new Empty();

        @Override
        public @Nullable TagKey<Object> key() {
            return null;
        }

        @Override
        public boolean contains(RegistryKey<Object> value) {
            return false;
        }

        @Override
        public Iterator<RegistryKey<Object>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /**
     * A tag that is backed by a registry.
     */
    static final class Backed<T> implements RegistryTag<T> {
        private final TagKey<T> key;
        private final Set<RegistryKey<T>> entries = new CopyOnWriteArraySet<>();

        Backed(TagKey<T> key) {
            this.key = key;
        }

        public TagKey<T> key() {
            return key;
        }

        @Override
        public boolean contains(RegistryKey<T> value) {
            return entries.contains(value instanceof RegistryKeyImpl<T> key ? key : new RegistryKeyImpl<>(value.key()));
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public Iterator<RegistryKey<T>> iterator() {
            return entries.iterator();
        }

        @ApiStatus.Internal
        void add(RegistryKey<T> key) {
            if (entries.add(key))
                invalidate();
        }

        @ApiStatus.Internal
        void remove(RegistryKey<T> key) {
            if (entries.remove(key))
                invalidate();
        }

        private void invalidate() {
            invalidationHook.run();
        }
    }

    record Direct<T>(List<RegistryKey<T>> keys) implements RegistryTag<T> {
        public Direct {
            keys = List.copyOf(keys);
        }

        @Override
        public @Nullable TagKey<T> key() {
            return null;
        }

        @Override
        public boolean contains(RegistryKey<T> value) {
            return keys.contains(value instanceof RegistryKeyImpl<T> key ? key : new RegistryKeyImpl<>(value.key()));
        }

        @Override
        public Iterator<RegistryKey<T>> iterator() {
            return keys.iterator();
        }

        @Override
        public int size() {
            return keys.size();
        }

        // Equality is defined by the underlying keys rather than the concrete RegistryKey
        // implementation, so a tag built from registry values (e.g. Material) is equal to an
        // equivalent tag read back from the network (which holds RegistryKeyImpl instances).
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RegistryTagImpl.Direct<?>(var keys1))
                    || keys.size() != keys1.size()) return false;
            for (int i = 0; i < keys.size(); i++)
                if (!keys.get(i).key().equals(keys1.get(i).key())) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (RegistryKey<T> key : keys)
                result = 31 * result + key.key().hashCode();
            return result;
        }
    }

}
