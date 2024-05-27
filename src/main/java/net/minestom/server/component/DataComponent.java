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

public sealed interface DataComponent<T> extends StaticProtocolObject permits DataComponentImpl {

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

    @ApiStatus.Internal
    static <T> DataComponent<T> register(@NotNull String name, @Nullable NetworkBuffer.Type<T> network, @Nullable BinaryTagSerializer<T> nbt) {
        DataComponent<T> impl = new DataComponentImpl<>(DataComponentImpl.NAMESPACES.size(), NamespaceID.from(name), network, nbt);
        DataComponentImpl.NAMESPACES.put(impl.name(), impl);
        DataComponentImpl.IDS.set(impl.id(), impl);
        return impl;
    }
}
