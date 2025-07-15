package net.minestom.server.adventure.provider;

import java.util.function.Consumer;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public final class MinestomGsonComponentSerializerProvider implements GsonComponentSerializer.Provider {
    @Override
    public GsonComponentSerializer gson() {
        return GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
                .build();
    }

    @Override
    public GsonComponentSerializer gsonLegacy() {
        return GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
                .downsampleColors()
                .build();
    }

    @Override
    public Consumer<GsonComponentSerializer.Builder> builder() {
        return builder -> {}; // we don't need to touch the builder here
    }
}
