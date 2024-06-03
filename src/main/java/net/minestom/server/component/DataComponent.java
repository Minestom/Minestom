package net.minestom.server.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A common type to represent all forms of component in the game. Each group of component types has its own declaration
 * file (see {@link net.minestom.server.item.ItemComponent} for example).
 *
 * @param <T> The value type of the component
 *
 * @see net.minestom.server.item.ItemComponent
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

    @NotNull T read(@NotNull BinaryTag tag);
    @NotNull BinaryTag write(@NotNull T value);

    @NotNull T read(@NotNull NetworkBuffer reader);
    void write(@NotNull NetworkBuffer writer, @NotNull T value);


    static @Nullable DataComponent<?> fromNamespaceId(@NotNull String namespaceId) {
        return DataComponentImpl.NAMESPACES.get(namespaceId);
    }

    static @Nullable DataComponent<?> fromNamespaceId(@NotNull NamespaceID namespaceId) {
        return fromNamespaceId(namespaceId.asString());
    }

    static @Nullable DataComponent<?> fromId(int id) {
        return DataComponentImpl.IDS.get(id);
    }

    static @NotNull Collection<DataComponent<?>> values() {
        return DataComponentImpl.NAMESPACES.values();
    }

    /**
     * Used to register new data component types. This should not be used externally, instead you should reference the
     * constants for existing component types, such as {@link net.minestom.server.item.ItemComponent}.
     */
    @ApiStatus.Internal
    static <T> DataComponent<T> register(@NotNull String name, @Nullable NetworkBuffer.Type<T> network, @Nullable BinaryTagSerializer<T> nbt) {
        DataComponent<T> impl = new DataComponentImpl<>(DataComponentImpl.NAMESPACES.size(), NamespaceID.from(name), network, nbt);
        DataComponentImpl.NAMESPACES.put(impl.name(), impl);
        DataComponentImpl.IDS.set(impl.id(), impl);
        return impl;
    }
}
