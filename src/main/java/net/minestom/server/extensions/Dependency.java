package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public sealed interface Dependency permits Dependency.ExtensionDependency, Dependency.MavenDependency {
    Logger LOGGER = LoggerFactory.getLogger(Dependency.class);

    static Dependency newExtensionDependency(@NotNull String name, @Nullable String version, boolean optional) {
        return new DependencyImpl.ExtensionDependency(name, version, optional);
    }

    static Dependency newMavenDependency(@NotNull String groupId, @NotNull String artifactId, @Nullable String version, boolean optional) {
        return new DependencyImpl.MavenDependency(groupId, artifactId, version, optional);
    }

    @NotNull String id();

    boolean isOptional();

    /**
     * An extension dependency specified in an <code>extension.json</code> file.
     */
    sealed interface ExtensionDependency extends Dependency permits DependencyImpl.ExtensionDependency {
        @Nullable String version();
    }

    sealed interface MavenDependency extends Dependency permits DependencyImpl.MavenDependency {
        @NotNull String groupId();

        @NotNull String artifactId();

        @NotNull String version();
    }

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
    static @Nullable Dependency fromJson(JsonElement json) {
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

            // Platform validation
            if (!isApplicableToCurrentPlatform(object)) {
                LOGGER.debug("Dependency {} is not applicable to current platform", id);
                return null;
            }
        } else {
            LOGGER.error("Invalid dependency format: " + json);
            return null;
        }

        // Create dependency
        String[] idSplit = id.split(":");
        return switch (idSplit.length) {
            case 1 -> newExtensionDependency(idSplit[0], null, optional);
            case 2 -> newExtensionDependency(idSplit[0], idSplit[1], optional);
            case 3 -> newMavenDependency(idSplit[0], idSplit[1], idSplit[2], optional);
            default -> {
                LOGGER.error("Invalid dependency format: {}", json.getAsString());
                yield null;
            }
        };
    }

    /**
     * Checks if a dependency is applicable on the current platform. Currently this
     * involves a check for the current OS and architecture.
     * <p>
     * OS Values: macos, windows, linux
     * Arch Values: x86, x86_64, arm64
     *
     * @param json The json to investigate
     * @return True if the dependency is applicable, false otherwise.
     */
    private static boolean isApplicableToCurrentPlatform(JsonObject json) {
        try {
            if (json.has("os")) {
                String os = json.get("os").getAsString();
                if (!os.equals(PlatformUtils.OS)) return false;
            }

            if (json.has("arch")) {
                String arch = json.get("arch").getAsString();
                if (!arch.equals(PlatformUtils.ARCH)) return false;
            }

            return true;
        } catch (Throwable exception) {
            LOGGER.warn("Failed to parse validate platform spec: " + json, exception);
            return false;
        }
    }
}
