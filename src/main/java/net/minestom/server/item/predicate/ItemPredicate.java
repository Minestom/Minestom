package net.minestom.server.item.predicate;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.instance.block.predicate.DataComponentPredicates;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.utils.Range;

import java.util.List;
import java.util.function.Predicate;

import static net.minestom.server.network.NetworkBuffer.NBT_COMPOUND;

public record ItemPredicate(
        List<Material> items,
        Range.Int count,
        DataComponentPredicates predicates
) implements Predicate<ItemStack> {

    public static final Codec<ItemPredicate> CODEC = StructCodec.struct(
            "items", Material.CODEC.listOrSingle().optional(), ItemPredicate::items,
            "count", DataComponentPredicates.INT_RANGE_CODEC.optional(), ItemPredicate::count,
            StructCodec.INLINE, DataComponentPredicates.CODEC.optional(), ItemPredicate::predicates,
            ItemPredicate::new
    );

    public static final NetworkBuffer.Type<ItemPredicate> NETWORK_TYPE = NBT_COMPOUND.transform(
            nbt -> {
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                return CODEC.decode(coder, nbt).orElseThrow();
            },
            predicate -> {
                final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
                BinaryTag tag = CODEC.encode(coder, predicate).orElseThrow();
                if (!(tag instanceof CompoundBinaryTag compound)) {
                    throw new IllegalStateException("Encoded ItemPredicate is not an NBT Compound");
                }
                return compound;
            }
    );

    public ItemPredicate(List<Material> items) {
        this(items, null, null);
    }

    public ItemPredicate(Range.Int count, List<Material> items) {
        this(items, count, null);
    }

    public ItemPredicate(DataComponentPredicates predicates) {
        this(null, null, predicates);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (items != null && !items.contains(itemStack.material()))
            return false;
        if (count != null && !count.inRange(itemStack.amount()))
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
