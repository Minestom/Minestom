package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
        public boolean contains(@NotNull RegistryKey<Object> value) {
            return false;
        }

        @Override
        public @NotNull Iterator<RegistryKey<Object>> iterator() {
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

        Backed(@NotNull TagKey<T> key) {
            this(key, Set.of());
        }

        Backed(@NotNull TagKey<T> key, @NotNull Set<RegistryKey<T>> entries) {
            this.key = key;
            this.entries = ServerFlag.REGISTRY_IMMUTABLE_TAGS ? Set.copyOf(entries) : new CopyOnWriteArraySet<>(entries);
        }

        @Override
        public @NotNull TagKey<T> key() {
            return key;
        }

        @Override
        public boolean contains(@NotNull RegistryKey<T> value) {
            return entries.contains(value instanceof RegistryKeyImpl<T> key ? key : new RegistryKeyImpl<>(value.key()));
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public @NotNull Iterator<RegistryKey<T>> iterator() {
            return entries.iterator();
        }

        @Override
        public boolean add(@NotNull RegistryKey<T> key) {
            var added = entries.add(key);
            if (added) invalidate();
            return added;
        }

        @Override
        public boolean remove(@NotNull RegistryKey<T> key) {
            boolean removed = entries.remove(key);
            if (removed) invalidate();
            return removed;
        }

        private void invalidate() {
            if (MinecraftServer.isInitializing()) return;
            MinecraftServer.getConnectionManager().invalidateTags();
        }
    }

    record Direct<T>(@NotNull List<RegistryKey<T>> keys) implements RegistryTag<T> {
        public Direct {
            keys = List.copyOf(keys);
        }

        @Override
        public @Nullable TagKey<T> key() {
            return null;
        }

        @Override
        public boolean contains(@NotNull RegistryKey<T> value) {
            return keys.contains(value instanceof RegistryKeyImpl<T> key ? key : new RegistryKeyImpl<>(value.key()));
        }

        @Override
        public @NotNull Iterator<RegistryKey<T>> iterator() {
            return keys.iterator();
        }

        @Override
        public int size() {
            return keys.size();
        }
    }

    static final class BuilderImpl<T> implements RegistryTag.Builder<T> {
        private final TagKey<T> key;
        private final List<RegistryKey<T>> entries = new ArrayList<>();

        BuilderImpl(@Nullable TagKey<T> key) {
            this.key = key;
        }

        public boolean add(@NotNull RegistryKey<T> key) {
            Check.notNull(key, "key");
            return entries.add(key);
        }

        public boolean remove(@NotNull RegistryKey<T> key) {
            Check.notNull(key, "key");
            return entries.remove(key);
        }

        RegistryTag<T> build() {
            if (entries.isEmpty()) {
                return RegistryTag.empty();
            } if (key != null) {
                return new Backed<>(key, Set.copyOf(entries));
            } else {
                return new Direct<T>(entries);
            }
        }
    }

}
