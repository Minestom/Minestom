package net.minestom.server.item;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;

public enum ItemAnimation {
    NONE,
    EAT,
    DRINK,
    BLOCK,
    BOW,
    SPEAR,
    CROSSBOW,
    SPYGLASS,
    TOOT_HORN,
    BRUSH,
    BUNDLE;

    public static final NetworkBuffer.Type<ItemAnimation> NETWORK_TYPE = NetworkBuffer.Enum(ItemAnimation.class);
    public static final BinaryTagSerializer<ItemAnimation> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(ItemAnimation.class);
}
