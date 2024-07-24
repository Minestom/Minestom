package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.item.enchant.EffectComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A common type to represent all forms of component in the game. Each group of component types has its own declaration
 * file (see {@link net.minestom.server.item.ItemComponent} for example).
 *
 * @param <T> The value type of the component
 *
 * @see net.minestom.server.item.ItemComponent
 * @see EffectComponent
 */
public sealed interface DataComponent<T> extends StaticProtocolObject permits DataComponentImpl {

    /**
     * Represents any type which can hold data components. Represents a finalized view of a component, that is to say
     * an implementation may represent a patch on top of another Holder, however the return values of this type
     * will always represent the merged view.
     */
    interface Holder {
        boolean has(@NotNull DataComponent<?> component);

        <T> @Nullable T get(@NotNull DataComponent<T> component);

        default <T> @NotNull T get(@NotNull DataComponent<T> component, @NotNull T defaultValue) {
            final T value = get(component);
            return value != null ? value : defaultValue;
        }
    }

    boolean isSynced();
    boolean isSerialized();

    @NotNull T read(@NotNull BinaryTagSerializer.Context context, @NotNull BinaryTag tag);
    @NotNull BinaryTag write(@NotNull BinaryTagSerializer.Context context, @NotNull T value);

    @NotNull T read(@NotNull NetworkBuffer reader);
    void write(@NotNull NetworkBuffer writer, @NotNull T value);

    @ApiStatus.Internal
    static <T> DataComponent<T> createHeadless(
            int id, @NotNull Key namespace,
            @Nullable NetworkBuffer.Type<T> network,
            @Nullable BinaryTagSerializer<T> nbt
    ) {
        return new DataComponentImpl<>(id, namespace, network, nbt);
    }
}
