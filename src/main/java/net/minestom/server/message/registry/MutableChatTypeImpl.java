package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

class MutableChatTypeImpl implements ChatType {
    private final Key key;
    private int id;

    public MutableChatTypeImpl(Key key) {
        this.key = key;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return NamespaceID.from(key);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
