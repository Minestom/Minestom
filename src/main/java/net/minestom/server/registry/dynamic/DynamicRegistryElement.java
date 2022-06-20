package net.minestom.server.registry.dynamic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.NBTRepresentable;
import net.minestom.server.registry.ProtocolObject;

public interface DynamicRegistryElement extends ProtocolObject, NBTRepresentable {
    Key CHAT_TYPE_REGISTRY = Key.key("minecraft:chat_type");

    Key registry();
}
