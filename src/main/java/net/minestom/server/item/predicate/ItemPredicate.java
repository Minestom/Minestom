package net.minestom.server.item.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.block.predicate.DataComponentPredicates;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record ItemPredicate(
        @Nullable List<Material> items,
        @Nullable Range.Int count,
        @Nullable DataComponentPredicates predicates
) implements Predicate<ItemStack> {

    public static final Codec<ItemPredicate> CODEC = StructCodec.struct(
            "items", Material.CODEC.listOrSingle().optional(), ItemPredicate::items,
            "count", DataComponentPredicates.INT_RANGE_CODEC.optional(), ItemPredicate::count,
            StructCodec.INLINE, DataComponentPredicates.CODEC.optional(), ItemPredicate::predicates,
            ItemPredicate::new
    );

    public static final NetworkBuffer.Type<ItemPredicate> NETWORK_TYPE = NetworkBuffer.TypedNBT(CODEC);

    public ItemPredicate(@NotNull List<Material> items) {
        this(items, null, null);
    }

    public ItemPredicate(@NotNull Range.Int count, @Nullable List<Material> items) {
        this(items, count, null);
    }

    public ItemPredicate(@NotNull DataComponentPredicates predicates) {
        this(null, null, predicates);
    }

    @Override
    public boolean test(@NotNull ItemStack itemStack) {
        if (items != null && !items.contains(itemStack.material()))
            return false;
        if (count != null && !count.inRange(itemStack.amount()))
            return false;

        return predicates == null || predicates.test(itemStack);
    }
}
