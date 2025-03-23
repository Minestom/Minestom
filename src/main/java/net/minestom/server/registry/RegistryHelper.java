package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    static <T extends ProtocolObject> void loadStaticJsonRegistry(@NotNull Registries registries, @NotNull DynamicRegistryImpl<T> registry, @NotNull Registry.Resource resource) {
        Check.argCondition(!resource.fileName().endsWith(".json"), "Resource must be a JSON file: {0}", resource.fileName());
        try (InputStream resourceStream = Registry.loadRegistryFile(resource)) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
            final JsonElement json = GSON.fromJson(new InputStreamReader(resourceStream, StandardCharsets.UTF_8), JsonElement.class);
            if (!(json instanceof JsonObject root))
                throw new IllegalStateException("Failed to load registry " + registry.id() + ": expected a JSON object, got " + json);

            final Transcoder<JsonElement> transcoder = new RegistryTranscoder<>(Transcoder.JSON, registries);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                final String namespace = entry.getKey();
                final Result<T> valueResult = registry.codec().decode(transcoder, entry.getValue());
                if (valueResult instanceof Result.Ok(T value)) {
                    registry.register(namespace, value, DataPack.MINECRAFT_CORE);
                } else {
                    throw new IllegalStateException("Failed to decode registry entry " + namespace + " for registry " + registry.id() + ": " + valueResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
