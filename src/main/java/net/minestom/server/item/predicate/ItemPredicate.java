package net.minestom.server.item.predicate;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.instance.block.predicate.Bounds;
import net.minestom.server.instance.block.predicate.DataComponentPredicates;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.function.Predicate;

public record ItemPredicate(
        List<Material> items,
        Bounds.IntBounds count,
        DataComponentPredicates predicates
) implements Predicate<ItemStack> {

    public static final Codec<ItemPredicate> CODEC = StructCodec.struct(
            "items", Material.CODEC.listOrSingle().optional(), ItemPredicate::items,
            "count", Bounds.IntBounds.CODEC.optional(Bounds.IntBounds.ANY), ItemPredicate::count,
            StructCodec.INLINE, DataComponentPredicates.CODEC.optional(), ItemPredicate::predicates,
            ItemPredicate::new
    );

    @Override
    public boolean test(ItemStack itemStack) {
        if (items != null && !items.contains(itemStack.material()))
            return false;
        if (count != null && !count.matches(itemStack.amount()))
            return false;
        if (predicates == null)
            return true;

        // Construct a DataComponentMap with ALL the item's components, including the default ones originating from the item's material
        DataComponentMap.Builder builder = itemStack.material().prototype().toBuilder();
        DataComponentMap patch = itemStack.componentPatch();

        for (DataComponent.Value value : patch.entrySet()) {
            copy(patch, builder, value.component());
        }

        return predicates.test(builder.build());
    }

    private <T> void copy(DataComponentMap source, DataComponentMap.Builder target, DataComponent<T> key) {
        if (source.has(key)) {
            T value = source.get(key);
            assert value != null;
            target.set(key, value);
        }
    }
}
