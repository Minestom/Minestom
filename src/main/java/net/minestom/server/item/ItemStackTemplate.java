package net.minestom.server.item;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

public interface ItemStackTemplate {
    NetworkBuffer.Type<ItemStack> NETWORK_TYPE = ItemStack.NETWORK_TYPE;
    Codec<ItemStack> CODEC = ItemStack.CODEC
            .orElse(Material.CODEC.transform(ItemStack::of, ItemStack::material));
}
