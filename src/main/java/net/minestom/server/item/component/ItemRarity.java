package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

public enum ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC;

    private static final ItemRarity[] VALUES = values();

    public static final NetworkBuffer.Type<ItemRarity> NETWORK_TYPE = NetworkBuffer.Enum(ItemRarity.class);
    public static final Codec<ItemRarity> CODEC = Codec.Enum(ItemRarity.class);
}
