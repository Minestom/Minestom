package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;

import java.util.function.Consumer;

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
                .editOptions(features -> features.value(JSONOptions.EMIT_RGB, false))
                .build();
    }

    @Override
    public Consumer<GsonComponentSerializer.Builder> builder() {
        return _ -> {}; // we don't need to touch the builder here
    }
}
