package net.minestom.server.item;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public interface ItemStackTemplate {
    NetworkBuffer.Type<ItemStack> NETWORK_TYPE = NetworkBufferTemplate.template(
            Material.NETWORK_TYPE, ItemStack::material,
            NetworkBuffer.VAR_INT, ItemStack::amount,
            DataComponent.PATCH_NETWORK_TYPE, (i) -> ((ItemStackImpl) i).components(),
            ItemStack::of);
    Codec<ItemStack> CODEC = ItemStack.CODEC
            .orElse(Material.CODEC.transform(ItemStack::of, ItemStack::material));
}
