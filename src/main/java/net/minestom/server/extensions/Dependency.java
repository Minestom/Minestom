package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Dependency permits Dependency.Extension, Dependency.Maven {

    static Dependency newExtensionDependency(@NotNull String name, @Nullable String version, boolean optional, @NotNull List<Maven> internalDependencies) {
        return new DependencyImpl.Extension(name, version, optional, internalDependencies);
    }

    static Dependency newMavenDependency(@NotNull String groupId, @NotNull String artifactId, @Nullable String version, @Nullable String classifier, boolean optional, @NotNull List<Maven> internalDependencies) {
        return new DependencyImpl.Maven(groupId, artifactId, version, classifier, optional, internalDependencies);
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

    @NotNull List<Maven> internalDependencies();

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
