package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

public interface NbtComponentSerializer extends ComponentSerializer<Component, Component, NBT> {
    static @NotNull NbtComponentSerializer nbt() {
        return NbtComponentSerializerImpl.INSTANCE;
    }
}
