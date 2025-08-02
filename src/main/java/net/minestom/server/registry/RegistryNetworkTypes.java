package net.minestom.server.registry;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.validate.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RegistryNetworkTypes {

    record RegistryKeyImpl<T>(Registries.Selector<T> selector) implements NetworkBuffer.Type<RegistryKey<T>> {
        @Override
        public void write(NetworkBuffer buffer, RegistryKey<T> value) {
            final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
            final var registry = selector.select(registries);
            final int id = registry.getId(value);
            Check.stateCondition(id == -1, "Key {0} is not registered in registry {1}", value, registry.key());
            buffer.write(NetworkBuffer.VAR_INT, id);
        }

        @Override
        public RegistryKey<T> read(NetworkBuffer buffer) {
            final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
            final var registry = selector.select(registries);
            final int id = buffer.read(NetworkBuffer.VAR_INT);
            final var key = registry.getKey(id);
            Check.stateCondition(key == null, "Unknown id {0} for registry {1}", id - 1, registry.key());
            return key;
        }
    }

    record HolderNetworkTypeImpl<T>(
            Registries.Selector<T> selector,
            NetworkBuffer.Type<T> registryNetworkType
    ) implements NetworkBuffer.Type<Holder<T>> {
        @Override
        public void write(NetworkBuffer buffer, Holder<T> value) {
            final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
            final var registry = selector.select(registries);
            switch (value.unwrap()) {
                case Either.Left(RegistryKey<T> key) -> {
                    final int id = registry.getId(key);
                    Check.stateCondition(id == -1, "Key {0} is not registered in registry {1}", key, registry.key());
                    buffer.write(NetworkBuffer.VAR_INT, id + 1);
                }
                case Either.Right(T direct) -> {
                    buffer.write(NetworkBuffer.VAR_INT, 0);
                    buffer.write(registryNetworkType, direct);
                }
            }
        }

        @Override
        public Holder<T> read(NetworkBuffer buffer) {
            final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
            final var registry = selector.select(registries);
            final int id = buffer.read(NetworkBuffer.VAR_INT);
            if (id == 0) //noinspection unchecked
                return (Holder<T>) buffer.read(registryNetworkType);

            final var key = registry.getKey(id - 1);
            Check.stateCondition(key == null, "Unknown id {0} for registry {1}", id - 1, registry.key());
            return key;
        }
    }

    record RegistryTagImpl<T>(Registries.Selector<T> selector) implements NetworkBuffer.Type<RegistryTag<T>> {
        @Override
        public void write(NetworkBuffer buffer, RegistryTag<T> value) {
            switch (value) {
                case net.minestom.server.registry.RegistryTagImpl.Backed<T> backed -> {
                    buffer.write(NetworkBuffer.VAR_INT, 0);
                    buffer.write(NetworkBuffer.KEY, backed.key().key());
                }
                case net.minestom.server.registry.RegistryTagImpl.Empty() -> buffer.write(NetworkBuffer.VAR_INT, 1);
                case net.minestom.server.registry.RegistryTagImpl.Direct(var entries) -> {
                    final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
                    final var registry = selector.select(registries);
                    buffer.write(NetworkBuffer.VAR_INT, entries.size() + 1);
                    for (RegistryKey<T> key : entries) {
                        final int id = registry.getId(key);
                        Check.stateCondition(id == -1, "Key {0} is not registered in registry {1}", key, registry.key());
                        buffer.write(NetworkBuffer.VAR_INT, id);
                    }
                }
            }
        }

        @Override
        public RegistryTag<T> read(NetworkBuffer buffer) {
            final var registries = Objects.requireNonNull(buffer.registries(), "Buffer is missing registries");
            final var registry = selector.select(registries);
            int count = buffer.read(NetworkBuffer.VAR_INT) - 1;
            if (count < 0) {
                final var key = buffer.read(NetworkBuffer.KEY);
                final var tag = registry.getTag(key);
                Check.stateCondition(tag == null, "No such tag {0} for registry {1}", key, registry.key());
                return tag;
            } else if (count == 0) {
                return RegistryTag.empty();
            } else {
                final List<RegistryKey<T>> keys = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    final int id = buffer.read(NetworkBuffer.VAR_INT);
                    final var key = registry.getKey(id);
                    Check.stateCondition(key == null, "Unknown id {0} for registry {1}", id, registry.key());
                    keys.add(key);
                }
                return new net.minestom.server.registry.RegistryTagImpl.Direct<>(keys);
            }
        }
    }

}
