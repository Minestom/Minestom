package net.minestom.server.network.debug;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.collection.ObjectArray;

import java.util.HashMap;
import java.util.Map;

public record DebugSubscriptionImpl<T>(
        int id,
        Key key,
        NetworkBuffer.Type<T> networkType
) implements DebugSubscription<T>, NetworkBuffer.Type<T> {
    static final Map<String, DebugSubscription<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DebugSubscription<?>> IDS = ObjectArray.singleThread(32);

    static {
        var ignoredForInit = DebugSubscriptions.BEES;
    }

    @Override
    public void write(NetworkBuffer buffer, T value) {
        networkType.write(buffer, value);
    }

    @Override
    public T read(NetworkBuffer buffer) {
        return networkType.read(buffer);
    }
}
