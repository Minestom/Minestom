package net.minestom.codegen;


import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

public record CodegenValue(Type codegenType, String namespace, String packageName, String typeName, String loaderName,
                           String generatedName, String keysName, Type registryType,
                           CodegenRegistry.Generator generator) {

    public CodegenValue {
        Objects.requireNonNull(codegenType, "Type cannot be null");
        Objects.requireNonNull(namespace, "Namespace cannot be null");
        Objects.requireNonNull(packageName, "Package name cannot be null");
        Objects.requireNonNull(typeName, "Type name cannot be null");
        Objects.requireNonNull(loaderName, "Loader name cannot be null");
        Objects.requireNonNull(generatedName, "Generated name cannot be null");
        Objects.requireNonNull(keysName, "Keys name cannot be null");
        Objects.requireNonNull(registryType, "Registry override cannot be null");
        Objects.requireNonNull(generator, "Generator cannot be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String tagsName() {
        return typeName() + "Tags";
    }

    public String resource() {
        return namespace();
    }

    public String tagResource() {
        return "tags/%s".formatted(namespace());
    }

    public MinestomCodeGenerator generator(Path path) {
        return generator.apply(path);
    }

    public enum Type {
        STATIC,
        DYNAMIC,
        SPECIAL // anything with its own codegen.
    }

    public static final class Builder {
        private @Nullable Type type;
        private @Nullable String namespace;
        private @Nullable String packageName;
        private @Nullable String typeName;
        private @Nullable String loaderName;
        private @Nullable String generatedName;
        private @Nullable String keyName;
        private @Nullable Type registryKeyOverride;
        private @Nullable CodegenRegistry.Generator generator;

        private Builder() {}

        public Builder namespace(String namespace) {
            this.namespace = Objects.requireNonNull(namespace);
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = Objects.requireNonNull(packageName);
            return this;
        }

        public Builder typeName(String typeName) {
            this.typeName = Objects.requireNonNull(typeName);
            return this;
        }

        public Builder generatedName(String generatedName) {
            this.generatedName = Objects.requireNonNull(generatedName);
            return this;
        }

        public Builder loaderName(String loaderName) {
            this.loaderName = loaderName;
            return this;
        }

        public Builder keyName(String keyName) {
            this.keyName = Objects.requireNonNull(keyName);
            return this;
        }

        public Builder registryKeyOverride(Type registryKeyOverride) {
            this.registryKeyOverride = Objects.requireNonNull(registryKeyOverride);
            return this;
        }

        public Builder staticType() {
            return type(Type.STATIC);
        }

        public Builder dynamicType() {
            return type(Type.DYNAMIC);
        }

        public Builder specialType() {
            return type(Type.SPECIAL);
        }

        public Builder generator(CodegenRegistry.Generator generator) {
            this.generator = Objects.requireNonNull(generator);
            return this;
        }

        public CodegenValue build() {
            Objects.requireNonNull(type, "Type must be set before building");
            Objects.requireNonNull(namespace, "Namespace must be set before building");
            Objects.requireNonNull(packageName, "Package name must be set before building");
            Objects.requireNonNull(typeName, "Type name must be set before building");
            Objects.requireNonNull(generator, "Generator name must be set before building");

            // Fill in defaults
            String loader = loaderName != null ? loaderName : switch (type) {
                case STATIC -> typeName + "Impl";
                case DYNAMIC, SPECIAL -> typeName;
            };
            String generated = generatedName != null ? generatedName : switch (type) {
                case STATIC, DYNAMIC -> typeName + "s";
                case SPECIAL -> typeName;
            };
            String keys = Objects.requireNonNullElse(keyName, typeName + "Keys");
            Type registryKey = Objects.requireNonNullElse(registryKeyOverride, type);
            return new CodegenValue(type, namespace, packageName, typeName, loader, generated, keys, registryKey, generator);
        }

        private Builder type(Type type) {
            if (this.type != null) throw new IllegalStateException("Type already set"); // probably a bug
            this.type = Objects.requireNonNull(type);
            return this;
        }
    }
}