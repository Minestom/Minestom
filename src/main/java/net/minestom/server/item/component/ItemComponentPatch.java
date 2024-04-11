package net.minestom.server.item.component;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemComponentPatch extends ItemComponentMap {

    static ItemComponentPatch EMPTY = new ItemComponentPatch() {
        @Override public boolean has(@NotNull ItemComponent<?> component) {
            return false;
        }

        @Override public <T> @Nullable T get(@NotNull ItemComponent<T> component) {
            return null;
        }
    }; //todo


    <T> @NotNull ItemComponentPatch with(@NotNull ItemComponent<T> component, T value);

    @NotNull ItemComponentPatch without(@NotNull ItemComponent<?> component);

    @NotNull Builder builder();

    interface Builder extends ItemComponentMap {

        @Contract(value = "_, _ -> this", pure = true)
        <T> @NotNull Builder set(@NotNull ItemComponent<T> component, @NotNull T value);

        @Contract(value = "_ -> this", pure = true)
        @NotNull Builder remove(@NotNull ItemComponent<?> component);

        @Contract(value = "-> new", pure = true)
        @NotNull ItemComponentPatch build();
    }
}
