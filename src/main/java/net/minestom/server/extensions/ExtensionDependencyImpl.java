package net.minestom.server.extensions;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record ExtensionDependencyImpl(
        @NotNull String id,
        @Nullable String version,
        boolean isOptional
) implements Dependency.ExtensionDependency {
    ExtensionDependencyImpl {
        Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
        Check.argCondition(!id.matches(ExtensionDescriptorImpl.NAME_REGEX), "Invalid extension name: " + id);
    }
}
