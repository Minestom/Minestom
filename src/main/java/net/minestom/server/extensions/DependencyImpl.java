package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.PlatformUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DependencyImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dependency.class);

    static @Nullable Dependency fromJson(@NotNull JsonElement json) {
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
            case 1 -> Dependency.newExtensionDependency(idSplit[0], null, optional);
            case 2 -> Dependency.newExtensionDependency(idSplit[0], idSplit[1], optional);
            case 3 -> Dependency.newMavenDependency(idSplit[0], idSplit[1], idSplit[2], null, optional);
            case 4 -> Dependency.newMavenDependency(idSplit[0], idSplit[1], idSplit[2], idSplit[3], optional);
            default -> {
                LOGGER.error("Invalid dependency format: {}", json.getAsString());
                yield null;
            }
        };
    }

    record Extension(String id, String version, boolean isOptional)
            implements Dependency.Extension {
        Extension {
            Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
            Check.argCondition(!id.matches(ExtensionDescriptorImpl.NAME_REGEX), "Invalid extension name: " + id);
        }
    }

    record Maven(String groupId, String artifactId, String version, String classifier, boolean isOptional)
            implements Dependency.Maven {
        @Override
        public @NotNull String id() {
            return artifactId();
        }
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
