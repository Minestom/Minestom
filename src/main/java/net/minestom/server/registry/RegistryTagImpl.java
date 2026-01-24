package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
        private final Set<RegistryKey<T>> entries;

        Backed(TagKey<T> key) {
            this.key = key;
            this.entries = new CopyOnWriteArraySet<>();
        }

        Backed(TagKey<T> key, Collection<RegistryKey<T>> entries) {
            this.key = key;
            this.entries = new CopyOnWriteArraySet<>(entries);
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

    static final class Builder<T> implements RegistryTag.Builder<T> {
        private final @Nullable TagKey<T> tagKey;
        private final Set<RegistryKey<T>> keys;

        public Builder(@Nullable TagKey<T> key) {
            this.tagKey = key;
            this.keys = new HashSet<>();
        }

        @Override
        public void add(RegistryKey<T> value) {
            this.keys.add(value);
        }

        @Override
        public void addAll(Collection<? extends RegistryKey<T>> values) {
            this.keys.addAll(values);
        }

        @Override
        public RegistryTag<T> build() {
            if (tagKey != null) return new Backed<>(tagKey, keys);
            if (keys.isEmpty()) return RegistryTag.empty();
            return RegistryTag.direct(keys);
        }
    }
}
