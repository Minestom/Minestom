package net.minestom.server.item;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

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
    public static final Codec<ItemAnimation> CODEC = Codec.Enum(ItemAnimation.class);
}
