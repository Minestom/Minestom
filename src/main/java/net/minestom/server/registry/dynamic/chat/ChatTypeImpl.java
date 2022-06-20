package net.minestom.server.registry.dynamic.chat;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

record ChatTypeImpl(int id, Key key, TextDisplay chat, TextDisplay overlay, Narration narration) implements ChatType {
    @Override
    public @NotNull NamespaceID namespace() {
        return NamespaceID.from(key);
    }
}
