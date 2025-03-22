package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.NotNull;

public final class RegistryHelper {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .disableJdkUnsafe()
            .create();

    public static <T> void register(@NotNull DynamicRegistry<T> registry, @NotNull String namespace, @NotNull String jsonString) {
        if (!(registry instanceof DynamicRegistryImpl<T> dynamicRegistry)) return;

        final JsonElement json = GSON.fromJson(jsonString, JsonElement.class);
        final T value = dynamicRegistry.codec().decode(Transcoder.JSON, json)
                .orElseThrow("failed to parse registry entry '" + namespace + "'");
        dynamicRegistry.register(namespace, value);
    }

}
