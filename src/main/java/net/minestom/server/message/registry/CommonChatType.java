package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.utils.FinalObject;
import org.jetbrains.annotations.ApiStatus;

public enum CommonChatType implements ChatType {
    CHAT(Key.key("minecraft:chat")),
    SYSTEM(Key.key("minecraft:system"));

    private final Key name;
    private final FinalObject<Integer> id = new FinalObject<>();

    CommonChatType(Key name) {
        this.name = name;
    }

    public boolean registered() {
        return id.isSet();
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id.set(id);
    }

    @Override
    public int id() {
        return this.id.get();
    }

    @Override
    public Key key() {
        return name;
    }

}
