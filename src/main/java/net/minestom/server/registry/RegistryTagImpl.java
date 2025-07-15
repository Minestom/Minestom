package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class RegistryTagImpl {

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
            var process = MinecraftServer.process();
            if (process == null) return;
            process.connection().invalidateTags();
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
    }

}
