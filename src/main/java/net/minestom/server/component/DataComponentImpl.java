package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

record DataComponentImpl<T>(
        int id,
        @NotNull Key key,
        @Nullable NetworkBuffer.Type<T> network,
        @Nullable Codec<T> codec
) implements DataComponent<T> {
    static final Map<Key, DataComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);

    static <T> DataComponent<T> register(@NotNull String name, @Nullable NetworkBuffer.Type<T> network, @Nullable Codec<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(NAMESPACES.size(), Key.key(name), network, nbt);
        NAMESPACES.put(impl.key(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }

    @Override
    public boolean isSynced() {
        return network != null;
    }

    @Override
    public boolean isSerialized() {
        return codec != null;
    }

    @Override
    public @NotNull <D> Result<T> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
        Check.notNull(codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.decode(coder, value);
    }

    @Override
    public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable T value) {
        Check.notNull(codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.encode(coder, value);
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
