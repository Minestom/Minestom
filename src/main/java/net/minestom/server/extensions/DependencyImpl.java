package net.minestom.server.extensions;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

final class DependencyImpl {
    record ExtensionDependency(String id, String version, boolean isOptional)
            implements Dependency.ExtensionDependency {
        ExtensionDependency {
            Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
            Check.argCondition(!id.matches(ExtensionDescriptorImpl.NAME_REGEX), "Invalid extension name: " + id);
        }
    }

    record MavenDependency(String groupId, String artifactId, String version, boolean isOptional)
            implements Dependency.MavenDependency {
        @Override
        public @NotNull String id() {
            return artifactId();
        }
    }
}
