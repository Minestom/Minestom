package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.PlatformUtil;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public sealed interface Dependency permits Dependency.ExtensionDependency, Dependency.MavenDependency {
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

    /**
     * An extension dependency specified in an <code>extension.json</code> file.
     */
    record ExtensionDependency(
            @NotNull String id,
            @Nullable String version,
            boolean isOptional
    ) implements Dependency {
        public ExtensionDependency {
            Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
            Check.argCondition(!id.matches(ExtensionDescriptor.NAME_REGEX), "Invalid extension name: " + id);
        }
    }

    record MavenDependency(
            String groupId,
            String artifactId,
            String version,
            boolean isOptional
    ) implements Dependency {
        @Override
        public String id() {
            return artifactId();
        }
    }

    /**
     * Checks if a dependency is applicable on the current platform. Currently this
     * involves a check for the current OS and architecture.
     *
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
                if (!os.equals(PlatformUtil.OS)) return false;
            }

            if (json.has("arch")) {
                String arch = json.get("arch").getAsString();
                if (!arch.equals(PlatformUtil.ARCH)) return false;
            }

            return true;
        } catch (Throwable exception) {
            LOGGER.warn("Failed to parse validate platform spec: " + json, exception);
            return false;
        }
    }
}
