package net.minestom.server.registry.dynamic;

import net.kyori.adventure.key.Key;

public interface DynamicRegistryEntryBuilder<T extends DynamicRegistryEntry> {
    Key registry();

    Key name();

    T build(int id);
}
