package net.minestom.server.extensions.descriptor;

import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * An extension dependency specified in an <code>extension.json</code> file.
 */
public record ExtensionDependency(
        @NotNull String id,
        @Nullable String version,
        boolean isOptional
) implements Dependency {

    public ExtensionDependency {
        Check.argCondition(id.isEmpty(), "Extension dependencies must have an id");
        Check.argCondition(!id.matches(ExtensionDescriptor.NAME_REGEX), "Invalid extension name: " + id);
        id = id.toLowerCase(Locale.ROOT);
    }


}
