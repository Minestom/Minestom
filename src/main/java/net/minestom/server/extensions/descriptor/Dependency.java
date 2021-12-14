package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public sealed interface Dependency permits ExtensionDependency, MavenDependency {
    Logger LOGGER = LoggerFactory.getLogger(Dependency.class);

    /**
     * Creates a {@link Dependency} from JSON.
     * <p>
     * If a dependency does not match the current system spec, <code>null</code> will be returned.
     * For example, the following would return null on a Windows system:
     * <code>
     *     {
     *          "id": "org.lwjgl:lwjgl:3.2.2-natives-macos-arm64",
     *          "os": "macos",
     *          "arch": "arm64"
     *     }
     * </code>
     *
     * @param json The json to parse
     * @return The created {@link Dependency}, or null if it could not be created.
     */
    @Nullable
    static Dependency fromJson(JsonElement json) {
        // Parse object
        String id;
        boolean optional = false;
        if (json.isJsonPrimitive()) {
            id = json.getAsString();
        } else if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            id = object.get("id").getAsString();
            if (object.has("optional")) {
                optional = object.get("optional").getAsBoolean();
            }
        } else {
            LOGGER.error("Invalid dependency format: " + json);
            return null;
        }

        // Create dependency
        String[] idSplit = id.split(":");
        return switch (idSplit.length) {
            case 1 -> new ExtensionDependency(idSplit[0], null, optional);
            case 2 -> new ExtensionDependency(idSplit[0], idSplit[1], optional);
            case 3 -> new MavenDependency(idSplit[0], idSplit[1], idSplit[2], optional);
            default -> {
                LOGGER.error("Invalid dependency format: {}", json.getAsString());
                yield null;
            }
        };
    }

    String id();

    boolean isOptional();
}
