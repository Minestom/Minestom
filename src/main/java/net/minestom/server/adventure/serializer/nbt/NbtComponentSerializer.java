package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minestom.server.registry.Registries;

import java.util.Objects;

public sealed interface NbtComponentSerializer extends ComponentSerializer<Component, Component, BinaryTag> permits NbtComponentSerializerImpl {
    /**
     * Returns a serializer that looks up the server process registries on every operation, so it fails when used
     * before the server process is initialized. Prefer {@link #nbt(Registries)} when a registry context is available.
     *
     * @return the serializer
     */
    static NbtComponentSerializer nbt() {
        return NbtComponentSerializerImpl.INSTANCE;
    }

    /**
     * Returns a serializer bound to the given registry context.
     *
     * @param registries the registry context used to resolve registry references
     * @return the serializer
     */
    static NbtComponentSerializer nbt(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return new NbtComponentSerializerImpl(registries);
    }
}
