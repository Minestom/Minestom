package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record DebugStickState(@NotNull Map<String, String> state) {
    public static final DebugStickState EMPTY = new DebugStickState(Map.of());

    public static final Codec<DebugStickState> CODEC = Codec.STRING.mapValue(Codec.STRING)
            .transform(DebugStickState::new, DebugStickState::state);
    public static final NetworkBuffer.Type<DebugStickState> NETWORK_TYPE = NetworkBuffer.TypedNBT(CODEC);

    public DebugStickState {
        state = Map.copyOf(state);
    }

    public @NotNull DebugStickState set(@NotNull String key, @NotNull String value) {
        Map<String, String> newState = new HashMap<>(state);
        newState.put(key, value);
        return new DebugStickState(newState);
    }

    public @NotNull DebugStickState remove(@NotNull String key) {
        Map<String, String> newState = new HashMap<>(state);
        newState.remove(key);
        return new DebugStickState(newState);
    }

}
