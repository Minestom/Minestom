package net.minestom.server.registry.dynamic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.NBTRepresentable;

public interface DynamicRegistryElementBuilder<T extends DynamicRegistryElement> extends NBTRepresentable {
    Key registry();
    DynamicRegistryElementFactory<T> factory();
}
