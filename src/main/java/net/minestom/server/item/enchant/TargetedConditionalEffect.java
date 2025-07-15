package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.condition.DataPredicate;
import org.jspecify.annotations.Nullable;

public record TargetedConditionalEffect<E extends Enchantment.Effect>(
        Enchantment.Target enchanted,
        Enchantment.@Nullable Target affected,
        E effect,
        @Nullable DataPredicate requirements
) implements Enchantment.Effect {

    public static <E extends Enchantment.Effect> Codec<TargetedConditionalEffect<E>> nbtType(Codec<E> effectType) {
        return StructCodec.struct(
                "enchanted", Enchantment.Target.CODEC, TargetedConditionalEffect::enchanted,
                "affected", Enchantment.Target.CODEC.optional(), TargetedConditionalEffect::affected,
                "effect", effectType, TargetedConditionalEffect::effect,
                "requirements", DataPredicate.NBT_TYPE.optional(), TargetedConditionalEffect::requirements,
                TargetedConditionalEffect::new
        );
    }

}
