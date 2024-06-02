package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record DebugStickState(@NotNull Map<String, String> state) {
    public static final DebugStickState EMPTY = new DebugStickState(Map.of());

    public static final BinaryTagSerializer<DebugStickState> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Map<String, String> state = new HashMap<>();
                for (Map.Entry<String, ? extends BinaryTag> entry : tag) {
                    if (!(entry.getValue() instanceof StringBinaryTag property))
                        continue;
                    state.put(entry.getKey(), property.value());
                }
                return new DebugStickState(state);
            },
            state -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Map.Entry<String, String> entry : state.state().entrySet()) {
                    builder.put(entry.getKey(), StringBinaryTag.stringBinaryTag(entry.getValue()));
                }
                return builder.build();
            }
    );

    public DebugStickState {
        state = Map.copyOf(state);
    }

}
