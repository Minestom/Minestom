package net.minestom.codegen;


import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Objects;

public sealed interface Entry {
    String namespace();
    String packageName();
    String typeName();
    String generatedName();

    default String tagsName() {
        return typeName() + "Tags";
    }

    default InputStream resource() {
        return Objects.requireNonNull(Generators.resource(namespace()), "Failed to locate resource for %s".formatted(namespace()));
    }

    default @Nullable InputStream tagResource() {
        return Generators.resource("tags/%s".formatted(namespace()));
    }


    record Static(String namespace, String packageName, String typeName, String loaderName, String generatedName, String keysName, boolean wildcardKey) implements Entry {
        Static(String namespace, String packageName, String typeName) {
            this(namespace, packageName, typeName, typeName + "Impl", typeName + "s", typeName + "Keys", false);
        }
        Static(String namespace, String packageName, String typeName, @Nullable String loaderName, @Nullable String generatedName) {
            this(namespace, packageName, typeName, Objects.requireNonNullElse(loaderName, typeName), Objects.requireNonNullElse(generatedName, typeName), typeName + "Keys", false);
        }
    }

    record Dynamic(String namespace, String packageName, String typeName, String generatedName, boolean ignoreKeys) implements Entry {
        public Dynamic(String namespace, String packageName, String typeName) {
            this(namespace, packageName, typeName, typeName + "s", false);
        }
    }
}
