package net.minestom.server.item;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

record ItemComponentImpl<T>(
        int id,
        @NotNull NamespaceID namespace,
        @Nullable NetworkBuffer.Type<T> network,
        @Nullable BinaryTagSerializer<T> nbt
) implements ItemComponent<T> {
    static final Map<String, ItemComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<ItemComponent<?>> IDS = ObjectArray.singleThread(32);

    static <T> ItemComponent<T> declare(@NotNull String name, @Nullable NetworkBuffer.Type<T> network, @Nullable BinaryTagSerializer<T> nbt) {
        ItemComponent<T> impl = new ItemComponentImpl<>(NAMESPACES.size(), NamespaceID.from(name), network, nbt);
        NAMESPACES.put(impl.name(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }

    @Override
    public @NotNull T read(@NotNull BinaryTag tag) {
        Check.notNull(nbt, "{0} cannot be deserialized from NBT", this);
        return nbt.read(tag);
    }

    @Override
    public @NotNull T read(@NotNull NetworkBuffer reader) {
        Check.notNull(network, "{0} cannot be deserialized from network", this);
        return network.read(reader);
    }

    @Override
    public String toString() {
        return name();
    }
}
