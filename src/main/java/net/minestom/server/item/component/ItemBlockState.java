package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ItemBlockState(@NotNull Map<String, String> properties) {
    public static final ItemBlockState EMPTY = new ItemBlockState(Map.of());

    public static final NetworkBuffer.Type<ItemBlockState> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING.mapValue(NetworkBuffer.STRING), ItemBlockState::properties,
            ItemBlockState::new
    );

    public static final BinaryTagSerializer<ItemBlockState> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                Map<String, String> properties = new HashMap<>(tag.size());
                for (Map.Entry<String, ? extends BinaryTag> entry : tag) {
                    if (!(entry.getValue() instanceof StringBinaryTag str)) continue;
                    properties.put(entry.getKey(), str.value());
                }
                return new ItemBlockState(properties);
            },
            value -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                for (Map.Entry<String, String> entry : value.properties.entrySet()) {
                    builder.put(entry.getKey(), StringBinaryTag.stringBinaryTag(entry.getValue()));
                }
                return builder.build();
            }
    );

    public ItemBlockState {
        properties = Map.copyOf(properties);
    }

    public @NotNull ItemBlockState with(@NotNull String key, @NotNull String value) {
        Map<String, String> newProperties = new HashMap<>(properties);
        newProperties.put(key, value);
        return new ItemBlockState(newProperties);
    }

    public @NotNull Block apply(@NotNull Block block) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (block.getProperty(entry.getKey()) == null)
                continue; // Ignore properties not present on this block
            block = block.withProperty(entry.getKey(), entry.getValue());
        }
        return block;
    }
}
