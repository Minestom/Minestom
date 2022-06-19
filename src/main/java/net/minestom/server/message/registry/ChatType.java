package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;

public interface ChatType {
    static ChatType of(int id, Key key) {
        return new ChatTypeImpl(id, key);
    }
    int id();
    Key key();

    record ChatTypeImpl(int id, Key key) implements ChatType {}
}
