package net.minestom.server.component;

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

record DataComponentImpl<T>(
        int id,
        @NotNull NamespaceID namespace,
        @Nullable NetworkBuffer.Type<T> network,
        @Nullable BinaryTagSerializer<T> nbt
) implements DataComponent<T> {
    static final Map<String, DataComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);


    @Override
    public @NotNull T read(@NotNull BinaryTag tag) {
        Check.notNull(nbt, "{0} cannot be deserialized from NBT", this);
        return nbt.read(tag);
    }

    @Override
    public @NotNull BinaryTag write(@NotNull T value) {
        Check.notNull(nbt, "{0} cannot be serialized to NBT", this);
        return nbt.write(value);
    }

    @Override
    public @NotNull T read(@NotNull NetworkBuffer reader) {
        Check.notNull(network, "{0} cannot be deserialized from network", this);
        return network.read(reader);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer, @NotNull T value) {
        Check.notNull(network, "{0} cannot be serialized to network", this);
        network.write(writer, value);
    }

    @Override
    public String toString() {
        return name();
    }
}
