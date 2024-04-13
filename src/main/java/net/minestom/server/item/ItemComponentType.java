package net.minestom.server.item;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface ItemComponentType<T> extends StaticProtocolObject permits ItemComponentTypeImpl {

    @NotNull T read(@NotNull BinaryTag tag);
    @NotNull BinaryTag write(@NotNull T value);

    @NotNull T read(@NotNull NetworkBuffer reader);
    void write(@NotNull NetworkBuffer writer, @NotNull T value);


    static @Nullable ItemComponentType<?> fromNamespaceId(@NotNull String namespaceId) {
        return ItemComponentTypeImpl.NAMESPACES.get(namespaceId);
    }

    static @Nullable ItemComponentType<?> fromNamespaceId(@NotNull NamespaceID namespaceId) {
        return fromNamespaceId(namespaceId.asString());
    }

    static @Nullable ItemComponentType<?> fromId(int id) {
        return ItemComponentTypeImpl.IDS.get(id);
    }
}
