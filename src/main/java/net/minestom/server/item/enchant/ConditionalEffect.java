package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.condition.DataPredicate;
import org.jspecify.annotations.Nullable;

public record ConditionalEffect<E extends Enchantment.Effect>(
        E effect,
        @Nullable DataPredicate requirements
) implements Enchantment.Effect {

    public static <E extends Enchantment.Effect> Codec<ConditionalEffect<E>> codec(Codec<E> effectType) {
        return StructCodec.struct(
                "effect", effectType, ConditionalEffect::effect,
                "requirements", DataPredicate.NBT_TYPE.optional(), ConditionalEffect::requirements,
                ConditionalEffect::new
        );
    }

}
