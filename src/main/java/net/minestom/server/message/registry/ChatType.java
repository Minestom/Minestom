package net.minestom.server.message.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.ProtocolObject;

public interface ChatType extends ProtocolObject {
    ChatType CHAT = new MutableChatTypeImpl(Key.key("minecraft:chat"));
    ChatType SYSTEM = new MutableChatTypeImpl(Key.key("minecraft:system"));

    static ChatType of(int id, Key key) {
        return new ChatTypeImpl(id, key);
    }

}
