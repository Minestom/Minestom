package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record DataComponentImpl<T>(
        int id,
        @NotNull Key key,
        @Nullable NetworkBuffer.Type<T> network,
        @Nullable BinaryTagSerializer<T> nbt
) implements DataComponent<T> {

    @Override
    public boolean isSynced() {
        return network != null;
    }

    @Override
    public boolean isSerialized() {
        return nbt != null;
    }

    @Override
    public @NotNull T read(@NotNull BinaryTagSerializer.Context context, @NotNull BinaryTag tag) {
        Check.notNull(nbt, "{0} cannot be deserialized from NBT", this);
        return nbt.read(context, tag);
    }

    @Override
    public @NotNull BinaryTag write(@NotNull BinaryTagSerializer.Context context, @NotNull T value) {
        Check.notNull(nbt, "{0} cannot be serialized to NBT", this);
        return nbt.write(context, value);
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
