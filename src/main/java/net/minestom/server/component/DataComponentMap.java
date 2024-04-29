package net.minestom.server.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataComponentMap {

    static @NotNull DataComponentMap.Builder builder() {
        return new DataComponentMapImpl.BuilderImpl(new Int2ObjectArrayMap<>());
    }

    boolean has(@NotNull DataComponent<?> component);

    <T> @Nullable T get(@NotNull DataComponent<T> component);

    default <T> @NotNull T get(@NotNull DataComponent<T> component, @NotNull T defaultValue) {
        T value = get(component);
        return value != null ? value : defaultValue;
    }

    interface Builder extends DataComponentMap {

        @NotNull Builder set(@NotNull DataComponent<?> component, @Nullable Object value);

        @NotNull Builder remove(@NotNull DataComponent<?> component);

        @NotNull DataComponentMap build();
    }

}
