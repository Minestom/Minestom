package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

public interface NbtComponentSerializer extends ComponentSerializer<Component, Component, BinaryTag> {
    static @NotNull NbtComponentSerializer nbt() {
        return NbtComponentSerializerImpl.INSTANCE;
    }
}
