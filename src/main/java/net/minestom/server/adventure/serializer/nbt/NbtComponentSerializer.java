package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.RegistryTranscoder;
import org.jetbrains.annotations.NotNull;

public interface NbtComponentSerializer extends ComponentSerializer<Component, Component, BinaryTag> {
    static @NotNull NbtComponentSerializer nbt() {
        return NbtComponentSerializerImpl.INSTANCE;
    }

    /**
     * @deprecated use {@link Codec#COMPONENT_STYLE} instead.
     */
    @Deprecated(forRemoval = true)
    default @NotNull Style deserializeStyle(@NotNull BinaryTag tag) {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        return Codec.COMPONENT_STYLE.decode(coder, tag).orElseThrow();
    }

    /**
     * @deprecated use {@link Codec#COMPONENT_STYLE} instead.
     */
    @Deprecated(forRemoval = true)
    default @NotNull CompoundBinaryTag serializeStyle(@NotNull Style style) {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        return (CompoundBinaryTag) Codec.COMPONENT_STYLE.encode(coder, style).orElseThrow();
    }
}
