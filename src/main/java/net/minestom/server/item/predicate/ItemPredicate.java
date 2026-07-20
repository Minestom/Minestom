package net.minestom.server.item.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.predicate.DataComponentPredicates;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record ItemPredicate(
        @Nullable RegistryTag<Material> items,
        @Nullable Range.Int count,
        @Nullable DataComponentPredicates predicates
) implements Predicate<ItemStack> {

    public static final Codec<ItemPredicate> CODEC = StructCodec.struct(
            "items", RegistryTag.codec(Registries::material).optional(), ItemPredicate::items,
            "count", Range.Int.CODEC.optional(), ItemPredicate::count,
            StructCodec.INLINE, DataComponentPredicates.CODEC.optional(), ItemPredicate::predicates,
            ItemPredicate::new
    );

    public static final NetworkBuffer.Type<ItemPredicate> NETWORK_TYPE = NetworkBuffer.TypedNBT(CODEC);

    public ItemPredicate(List<Material> items) {
        this(RegistryTag.direct(items.stream().map(Material::registryKey).toList()), null, null);
    }

    public ItemPredicate(Range.Int count, @Nullable List<Material> items) {
        this(items == null ? null : RegistryTag.direct(
                items.stream().map(Material::registryKey).toList()), count, null);
    }

    public ItemPredicate(DataComponentPredicates predicates) {
        this(null, null, predicates);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (items != null && !items.contains(itemStack.material().registryKey()))
            return false;
        if (count != null && !count.inRange(itemStack.amount()))
            return false;

        return predicates == null || predicates.test(itemStack);
    }
}
