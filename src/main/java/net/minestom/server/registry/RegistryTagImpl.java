package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
    static final class Backed<T> implements RegistryTag<T>, RegistryTag.Builder<T> {
        private final TagKey<T> key;
        private final Set<RegistryKey<T>> entries;

        Backed(TagKey<T> key) {
            this(key, Set.of());
        }

        Backed(TagKey<T> key, Collection<RegistryKey<T>> entries) {
            this.key = key;
            this.entries = ServerFlag.REGISTRY_FREEZING_TAGS ? Set.copyOf(entries) : new CopyOnWriteArraySet<>(entries);
        }

        @Override
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

        @Override
        public boolean add(RegistryKey<T> key) {
            var added = entries.add(key);
            if (added) invalidate();
            return added;
        }

        @Override
        public boolean remove(RegistryKey<T> key) {
            boolean removed = entries.remove(key);
            if (removed) invalidate();
            return removed;
        }

        private void invalidate() {
            MinecraftServer.getConnectionManager().invalidateTags();
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

    static final class BuilderImpl<T> implements RegistryTag.Builder<T> {
        private final @Nullable TagKey<T> key;
        private final List<RegistryKey<T>> entries;

        BuilderImpl(@Nullable TagKey<T> key) {
            this.key = key;
            this.entries = new ArrayList<>();
        }

        public boolean add(RegistryKey<T> key) {
            Check.notNull(key, "Registry key cannot be null");
            return entries.add(key);
        }

        public boolean remove(RegistryKey<T> key) {
            Check.notNull(key, "Registry key cannot be null");
            return entries.remove(key);
        }

        RegistryTag<T> build() {
            if (key != null) {
                return new Backed<>(key, entries);
            } else if (entries.isEmpty()) {
                return RegistryTag.empty();
            }  else {
                return new Direct<T>(entries);
            }
        }
    }

}
