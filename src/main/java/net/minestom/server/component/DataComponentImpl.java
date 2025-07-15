package net.minestom.server.component;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

record DataComponentImpl<T>(
        int id,
        Key key,
        NetworkBuffer.@Nullable Type<T> network,
        @Nullable Codec<T> codec
) implements DataComponent<T> {
    static final Map<String, DataComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);

    static <T> DataComponent<T> register(String name, NetworkBuffer.@Nullable Type<T> network, @Nullable Codec<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(NAMESPACES.size(), Key.key(name), network, nbt);
        NAMESPACES.put(impl.name(), impl);
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
    public <D> Result<T> decode(Transcoder<D> coder, D value) {
        Check.notNull(codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.decode(coder, value);
    }

    @Override
    public <D> Result<D> encode(Transcoder<D> coder, @Nullable T value) {
        Check.notNull(codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.encode(coder, value);
    }

    @Override
    public T read(NetworkBuffer reader) {
        Check.notNull(network, "{0} cannot be deserialized from network", this);
        return network.read(reader);
    }

    @Override
    public void write(NetworkBuffer writer, T value) {
        Check.notNull(network, "{0} cannot be serialized to network", this);
        network.write(writer, value);
    }

    @Override
    public String toString() {
        return name();
    }
}
