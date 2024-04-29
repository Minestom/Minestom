package net.minestom.server.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DataComponentMapImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap {
    @Override
    public boolean has(@NotNull DataComponent<?> component) {
        return components.get(component.id()) != null;
    }

    @Override
    public <T> @Nullable T get(@NotNull DataComponent<T> component) {
        return (T) components.get(component.id());
    }

    public record BuilderImpl(@NotNull Int2ObjectMap<Object> components) implements DataComponentMap.Builder {

        @Override
        public boolean has(@NotNull DataComponent<?> component) {
            return components.get(component.id()) != null;
        }

        @Override
        public <T> @Nullable T get(@NotNull DataComponent<T> component) {
            return (T) components.get(component.id());
        }

        @Override
        public @NotNull Builder set(@NotNull DataComponent<?> component, @Nullable Object value) {
            components.put(component.id(), value);
            return this;
        }

        @Override
        public @NotNull Builder remove(@NotNull DataComponent<?> component) {
            components.remove(component.id());
            return this;
        }

        @Override
        public @NotNull DataComponentMap build() {
            return new DataComponentMapImpl(components);
        }
    }
}
