package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public sealed interface Dependency permits Dependency.Extension, Dependency.Maven {

    static Dependency newExtensionDependency(@NotNull String name, @Nullable String version, boolean optional) {
        return new DependencyImpl.Extension(name, version, optional);
    }

    static Dependency newMavenDependency(@NotNull String groupId, @NotNull String artifactId, @Nullable String version, @Nullable String classifier, boolean optional) {
        return new DependencyImpl.Maven(groupId, artifactId, version, classifier, optional);
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
    static @Nullable Dependency fromJson(@NotNull JsonElement json) {
        return DependencyImpl.fromJson(json);
    }

    @NotNull String id();

    boolean isOptional();

    /**
     * An extension dependency specified in an <code>extension.json</code> file.
     */
    sealed interface Extension extends Dependency permits DependencyImpl.Extension {
        @Nullable String version();
    }

    sealed interface Maven extends Dependency permits DependencyImpl.Maven {
        @NotNull String groupId();

        @NotNull String artifactId();

        @NotNull String version();

        @Nullable String classifier();
    }

}
