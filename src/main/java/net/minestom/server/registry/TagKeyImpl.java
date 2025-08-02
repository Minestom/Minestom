package net.minestom.server.registry;

import net.kyori.adventure.key.Key;

record TagKeyImpl<T>(Key key) implements TagKey<T> {
}
