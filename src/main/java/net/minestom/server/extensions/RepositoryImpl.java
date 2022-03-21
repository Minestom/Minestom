package net.minestom.server.extensions;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

record RepositoryImpl(
        @NotNull String id,
        @NotNull String url
) implements Repository {

    private static final Map<String, Repository> PREDEFINED_REPOSITORIES = Map.of(
            "mavencentral", new RepositoryImpl("MavenCentral", "https://repo1.maven.org/maven2/"),
            "jitpack", new RepositoryImpl("JitPack", "https://jitpack.io/")
    );

    //todo weird public loggers.
    static @NotNull Repository fromJson(@NotNull JsonElement json) {
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            Check.argCondition(!object.has("id"), "Repository must have an id");
            Check.argCondition(!object.has("url"), "Repository must have a url");

            String id = object.get("id").getAsString();
            String url = object.get("url").getAsString();

            return new RepositoryImpl(id, url);
        } else if (json.isJsonPrimitive()) {
            String name = json.getAsString().toLowerCase(Locale.ROOT);
            Check.argCondition(PREDEFINED_REPOSITORIES.containsKey(name), "Unknown repository '" + name + "'.");

            return PREDEFINED_REPOSITORIES.get(name);
        }

        throw new IllegalArgumentException("Repository definitions must be strings or objects, not: " + json);
    }
}
