package net.minestom.server.extensions;


import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

record RepositoryImpl(
        @NotNull String id,
        @NotNull String url
) implements Repository {
    private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);

    //todo weird public loggers.
    static @NotNull Repository fromJson(@NotNull JsonObject json) {
        Check.argCondition(!json.has("id"), "Repository must have an id");
        Check.argCondition(!json.has("url"), "Repository must have a url");

        String id = json.get("id").getAsString();
        String url = json.get("url").getAsString();
        return new RepositoryImpl(id, url);
    }
}
