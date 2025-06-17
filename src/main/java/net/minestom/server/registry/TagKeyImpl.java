package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

record TagKeyImpl<T>(@NotNull Key key) implements TagKey<T> {
}
