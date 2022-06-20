package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

record ChatTypeImpl(int id, Key key) implements ChatType {
    @Override
    public @NotNull NamespaceID namespace() {
        return NamespaceID.from(key);
    }
}
