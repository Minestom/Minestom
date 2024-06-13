package net.minestom.server.item.enchant;

import net.minestom.server.condition.DataPredicate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ConditionalEffect<E extends Enchantment.Effect>(
        @NotNull E effect,
        @Nullable DataPredicate requirements
) implements Enchantment.Effect {

    public static <E extends Enchantment.Effect> @NotNull BinaryTagSerializer<ConditionalEffect<E>> nbtType(@NotNull BinaryTagSerializer<E> effectType) {
        return BinaryTagSerializer.object(
                "effect", effectType, ConditionalEffect::effect,
                "requirements", DataPredicate.NBT_TYPE.optional(), ConditionalEffect::requirements,
                ConditionalEffect::new
        );
    }

}
