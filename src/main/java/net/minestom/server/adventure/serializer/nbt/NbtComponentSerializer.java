package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;

public sealed interface NbtComponentSerializer extends ComponentSerializer<Component, Component, BinaryTag> permits NbtComponentSerializerImpl {
    static NbtComponentSerializer nbt() {
        return NbtComponentSerializerImpl.INSTANCE;
    }
}
