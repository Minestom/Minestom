package net.minestom.server.adventure.provider;

import java.util.function.Consumer;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // we are permitted to provide this
public final class MinestomGsonComponentSerializerProvider implements GsonComponentSerializer.Provider {
    @Override
    public @NotNull GsonComponentSerializer gson() {
        return GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
                .build();
    }

    @Override
    public @NotNull GsonComponentSerializer gsonLegacy() {
        return GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.INSTANCE)
                .downsampleColors()
                .build();
    }

    @Override
    public @NotNull Consumer<GsonComponentSerializer.Builder> builder() {
        return builder -> {}; // we don't need to touch the builder here
    }
}
