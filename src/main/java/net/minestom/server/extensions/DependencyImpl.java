package net.minestom.server.extensions;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

final class DependencyImpl {
    record Extension(String id, String version, boolean isOptional)
            implements Dependency.Extension {
        Extension {
            Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
            Check.argCondition(!id.matches(ExtensionDescriptorImpl.NAME_REGEX), "Invalid extension name: " + id);
        }
    }

    record Maven(String groupId, String artifactId, String version, boolean isOptional)
            implements Dependency.Maven {
        @Override
        public @NotNull String id() {
            return artifactId();
        }
    }
}
