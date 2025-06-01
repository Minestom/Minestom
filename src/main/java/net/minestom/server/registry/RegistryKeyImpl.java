package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

record RegistryKeyImpl<T>(@NotNull Key key) implements RegistryKey<T> {
}
