package net.minestom.server.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemComponentMap {

    static @NotNull Builder builder() {
        //todo
    }

    boolean has(@NotNull ItemComponent<?> component);

    <T> @Nullable T get(@NotNull ItemComponent<T> component);

    default <T> @NotNull T get(@NotNull ItemComponent<T> component, @NotNull T defaultValue) {
        T value = get(component);
        return value != null ? value : defaultValue;
    }

    interface Builder {

        <T> @NotNull Builder set(@NotNull ItemComponent<T> component, @NotNull T value);

        void remove(@NotNull ItemComponent<?> component);

        @NotNull ItemComponentMap build();
    }
}
