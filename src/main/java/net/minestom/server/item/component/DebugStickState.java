package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

import java.util.HashMap;
import java.util.Map;

public record DebugStickState(Map<String, String> state) {
    public static final DebugStickState EMPTY = new DebugStickState(Map.of());

    public static final Codec<DebugStickState> CODEC = Codec.STRING.mapValue(Codec.STRING)
            .transform(DebugStickState::new, DebugStickState::state);
    public static final NetworkBuffer.Type<DebugStickState> NETWORK_TYPE = NetworkBuffer.TypedNBT(CODEC);

    public DebugStickState {
        state = Map.copyOf(state);
    }

    public DebugStickState set(String key, String value) {
        Map<String, String> newState = new HashMap<>(state);
        newState.put(key, value);
        return new DebugStickState(newState);
    }

    public DebugStickState remove(String key) {
        Map<String, String> newState = new HashMap<>(state);
        newState.remove(key);
        return new DebugStickState(newState);
    }

}
