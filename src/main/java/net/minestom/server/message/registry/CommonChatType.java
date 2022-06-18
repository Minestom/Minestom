package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

public enum CommonChatType {
    CHAT(Key.key("minecraft:chat")),
    SYSTEM(Key.key("minecraft:system"));

    private final Key name;
    private int id;

    CommonChatType(Key name) {
        this.name = name;
    }

    public Key getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean registered() {
        return getId() != -1;
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id = id;
    }
}
