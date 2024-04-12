package net.minestom.server.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemComponentMap {

    static @NotNull ItemComponentMap.Builder builder() {
        return new ItemComponentMapImpl.BuilderImpl(new Int2ObjectArrayMap<>());
    }

    boolean has(@NotNull ItemComponent<?> component);

    <T> @Nullable T get(@NotNull ItemComponent<T> component);

    default <T> @NotNull T get(@NotNull ItemComponent<T> component, @NotNull T defaultValue) {
        T value = get(component);
        return value != null ? value : defaultValue;
    }

    interface Builder extends ItemComponentMap {

        @NotNull Builder set(@NotNull ItemComponent<?> component, @Nullable Object value);

        @NotNull Builder remove(@NotNull ItemComponent<?> component);

        @NotNull ItemComponentMap build();
    }

}
