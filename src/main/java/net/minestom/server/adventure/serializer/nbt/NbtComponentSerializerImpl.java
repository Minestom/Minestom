package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTranscoder;
import org.jetbrains.annotations.Nullable;

record NbtComponentSerializerImpl(@Nullable Registries registries) implements NbtComponentSerializer {
    static final NbtComponentSerializer INSTANCE = new NbtComponentSerializerImpl(null);

    @Override
    public Component deserialize(BinaryTag input) {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, context());
        return Codec.COMPONENT.decode(coder, input).orElseThrow();
    }

    @Override
    public BinaryTag serialize(Component component) {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, context());
        return Codec.COMPONENT.encode(coder, component).orElseThrow();
    }

    private Registries context() {
        return registries != null ? registries : MinecraftServer.getRegistries();
    }
}
