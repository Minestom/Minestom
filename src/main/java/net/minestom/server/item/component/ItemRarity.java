package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

public enum ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC;

    private static final ItemRarity[] VALUES = values();

    public static final NetworkBuffer.Type<ItemRarity> NETWORK_TYPE = NetworkBuffer.Enum(ItemRarity.class);
    public static final BinaryTagSerializer<ItemRarity> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(ItemRarity.class);
}
