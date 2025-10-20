package net.minestom.codegen;

import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record CodegenRegistry(Map<String, CodegenValue> registry, ResourceFunction resource) {
    public CodegenRegistry {
        registry = Map.copyOf(registry);
        Objects.requireNonNull(resource, "Resource function cannot be null");
    }

    public static Builder builder(ResourceFunction resource) {
        return new Builder(resource);
    }

    public CodegenValue get(String value) {
        return Objects.requireNonNull(registry.get(value), "Cannot find registry entry: %s".formatted(value));
    }

    InputStreamReader resource(String name) {
        return Objects.requireNonNull(resource.apply(name), "Cannot find resource: %s".formatted(name));
    }

    @Nullable InputStreamReader optionalResource(String name) {
        return resource.apply(name);
    }

    public static final class Builder {
        private final Map<String, CodegenValue> registry = new HashMap<>();
        private final ResourceFunction resource;

        private Builder(ResourceFunction resource) {
            this.resource = Objects.requireNonNull(resource, "Resource function cannot be null");;
        }

        public Builder put(CodegenValue value) {
            this.registry.put(value.namespace(), value);
            return this;
        }

        public Builder putAll(CodegenValue... values) {
            return putAll(List.of(values));
        }

        public Builder putAll(Collection<CodegenValue> registry) {
            return putAll(registry.stream().collect(Collectors.toUnmodifiableMap(CodegenValue::namespace, Function.identity())));
        }

        public Builder putAll(Map<String, CodegenValue> registry) {
            this.registry.putAll(registry);
            return this;
        }

        public CodegenRegistry build() {
            return new CodegenRegistry(registry, resource);
        }
    }

    @FunctionalInterface
    public interface ResourceFunction extends Function<String, @Nullable InputStreamReader> {}
}
