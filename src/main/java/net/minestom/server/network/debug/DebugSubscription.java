package net.minestom.server.network.debug;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface DebugSubscription<T> extends StaticProtocolObject<DebugSubscription<T>>, DebugSubscriptions permits DebugSubscriptionImpl {

    NetworkBuffer.Type<DebugSubscription<?>> NETWORK_TYPE = NetworkBuffer.VAR_INT
            .transform(DebugSubscription::fromId, DebugSubscription::id);

    int id();

    Key key();

    T read(NetworkBuffer reader);
    void write(NetworkBuffer writer, T value);


    static @Nullable DebugSubscription<?> fromKey(String key) {
        return DebugSubscriptionImpl.NAMESPACES.get(key);
    }

    static @Nullable DebugSubscription<?> fromKey(Key key) {
        return fromKey(key.asString());
    }

    static @Nullable DebugSubscription<?> fromId(int id) {
        return DebugSubscriptionImpl.IDS.get(id);
    }

    static Collection<DebugSubscription<?>> values() {
        return DebugSubscriptionImpl.NAMESPACES.values();
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    record Event<T>(DebugSubscription<T> subscription, T value) {
        public static final NetworkBuffer.Type<DebugSubscription.Event<?>> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Event value) {
                buffer.write(DebugSubscription.NETWORK_TYPE, value.subscription);
                value.subscription.write(buffer, value.value);
            }

            @Override
            public Event<?> read(NetworkBuffer buffer) {
                DebugSubscription<?> subscription = buffer.read(DebugSubscription.NETWORK_TYPE);
                return new Event(subscription, subscription.read(buffer));
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    record Update<T>(DebugSubscription<T> subscription, @Nullable T value) {
        public static final NetworkBuffer.Type<DebugSubscription.Update<?>> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Update value) {
                buffer.write(DebugSubscription.NETWORK_TYPE, value.subscription);
                buffer.write(NetworkBuffer.BOOLEAN, value.value != null);
                if (value.value != null) value.subscription.write(buffer, value.value);
            }

            @Override
            public Update<?> read(NetworkBuffer buffer) {
                DebugSubscription<?> subscription = buffer.read(DebugSubscription.NETWORK_TYPE);
                boolean hasValue = buffer.read(NetworkBuffer.BOOLEAN);
                Object value = hasValue ? subscription.read(buffer) : null;
                return new Update(subscription, value);
            }
        };
    }

}
