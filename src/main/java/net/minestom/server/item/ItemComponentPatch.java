package net.minestom.server.item;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface ItemComponentPatch extends ItemComponentMap permits ItemComponentPatchImpl {

    @NotNull NetworkBuffer.Type<ItemComponentPatch> NETWORK_TYPE = null;
    @NotNull BinaryTagSerializer<ItemComponentPatch> NBT_TYPE = null;

    <T> @NotNull ItemComponentPatch with(@NotNull ItemComponent<T> component, T value);

    @NotNull ItemComponentPatch without(@NotNull ItemComponent<?> component);

    interface Builder extends ItemComponentMap {

        @Contract(value = "_, _ -> this", pure = true)
        <T> @NotNull Builder set(@NotNull ItemComponent<T> component, @NotNull T value);

        @Contract(value = "_ -> this", pure = true)
        @NotNull Builder remove(@NotNull ItemComponent<?> component);

        @Contract(value = "-> new", pure = true)
        @NotNull ItemComponentPatch build();
    }
}
