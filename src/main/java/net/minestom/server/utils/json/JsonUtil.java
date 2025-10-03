package net.minestom.server.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;

import java.io.Reader;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().disableJdkUnsafe().setStrictness(Strictness.STRICT).create();

    public static JsonElement fromJson(String json) {
        return GSON.fromJson(json, JsonElement.class);
    }

    public static JsonElement fromJson(Reader reader) {
        return GSON.fromJson(reader, JsonElement.class);
    }

    public static String toJson(JsonElement element) {
        return GSON.toJson(element);
    }

    private JsonUtil() {
    }
}
