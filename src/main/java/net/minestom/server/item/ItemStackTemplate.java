package net.minestom.server.item;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;

public interface ItemStackTemplate {
    NetworkBuffer.Type<ItemStack> NETWORK_TYPE = NetworkBufferTemplate.template(
            Material.NETWORK_TYPE, ItemStack::material,
            NetworkBuffer.VAR_INT, ItemStack::amount,
            DataComponent.PATCH_NETWORK_TYPE, ItemStack::components,
            ItemStack::of);

    Codec<ItemStack> CODEC = StructCodec.struct(
                    "id", Material.CODEC, ItemStack::material,
                    "count", Codec.INT.transform(ItemStackTemplate::checkCount, count -> count).optional(1), ItemStack::amount,
                    "components", DataComponent.PATCH_CODEC.optional(DataComponentMap.EMPTY), ItemStack::componentPatch,
                    ItemStack::of)
            .orElse(Material.CODEC.transform(ItemStack::of, ItemStack::material));

    private static int checkCount(int count) {
        Check.argCondition(count < 1 || count > 99, "Count must be between 1 and 99, got {0}", count);
        return count;
    }
}
