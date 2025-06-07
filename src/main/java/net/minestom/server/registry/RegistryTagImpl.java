package net.minestom.server.registry;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class RegistryTagImpl {

    record Empty() implements RegistryTag<Object> {
        public static final Empty INSTANCE = new Empty();

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
    static final class Backed<T> implements RegistryTag<T> {
        private final TagKey<T> key;
        private final Set<RegistryKey<T>> entries = new CopyOnWriteArraySet<>();

        Backed(@NotNull TagKey<T> key) {
            this.key = key;
        }

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

        @ApiStatus.Internal
        void add(@NotNull RegistryKey<T> key) {
            entries.add(key);
            invalidate();
        }

        @ApiStatus.Internal
        void remove(@NotNull RegistryKey<T> key) {
            entries.remove(key);
            invalidate();
        }

        private void invalidate() {
            var process = MinecraftServer.process();
            if (process == null) return;
            process.connection().invalidateTags();
        }
    }

    record Direct<T>(@NotNull List<RegistryKey<T>> keys) implements RegistryTag<T> {
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

}
