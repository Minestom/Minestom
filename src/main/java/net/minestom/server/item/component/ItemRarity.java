package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC;

    private static final Map<String, ItemRarity> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(v -> v.name().toLowerCase(), Function.identity()));

    public static final NetworkBuffer.Type<ItemRarity> NETWORK_TYPE = NetworkBuffer.Enum(ItemRarity.class);
    public static final Codec<ItemRarity> CODEC = Codec.STRING.transform(BY_ID::get, v -> v.name().toLowerCase());
}
