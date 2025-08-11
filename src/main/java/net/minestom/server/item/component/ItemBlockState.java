package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;

import java.util.HashMap;
import java.util.Map;

public record ItemBlockState(Map<String, String> properties) {
    public static final ItemBlockState EMPTY = new ItemBlockState(Map.of());

    public static final NetworkBuffer.Type<ItemBlockState> NETWORK_TYPE = NetworkBuffer.STRING.mapValue(NetworkBuffer.STRING)
            .transform(ItemBlockState::new, ItemBlockState::properties);
    public static final Codec<ItemBlockState> CODEC = Codec.STRING.mapValue(Codec.STRING)
            .transform(ItemBlockState::new, ItemBlockState::properties);

    public ItemBlockState {
        properties = Map.copyOf(properties);
    }

    public ItemBlockState with(String key, String value) {
        Map<String, String> newProperties = new HashMap<>(properties);
        newProperties.put(key, value);
        return new ItemBlockState(newProperties);
    }

    public Block apply(Block block) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (block.getProperty(entry.getKey()) == null)
                continue; // Ignore properties not present on this block
            block = block.withProperty(entry.getKey(), entry.getValue());
        }
        return block;
    }
}
