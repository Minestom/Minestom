package net.minestom.server.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemComponentMap {

    static @NotNull ItemComponentMap.Builder builder() {
        return new ItemComponentMapImpl.BuilderImpl(new Int2ObjectArrayMap<>());
    }

    boolean has(@NotNull ItemComponentType<?> component);

    <T> @Nullable T get(@NotNull ItemComponentType<T> component);

    default <T> @NotNull T get(@NotNull ItemComponentType<T> component, @NotNull T defaultValue) {
        T value = get(component);
        return value != null ? value : defaultValue;
    }

    interface Builder extends ItemComponentMap {

        @NotNull Builder set(@NotNull ItemComponentType<?> component, @Nullable Object value);

        @NotNull Builder remove(@NotNull ItemComponentType<?> component);

        @NotNull ItemComponentMap build();
    }

}
