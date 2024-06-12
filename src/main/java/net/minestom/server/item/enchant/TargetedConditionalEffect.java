package net.minestom.server.item.enchant;

import net.minestom.server.condition.DataPredicate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TargetedConditionalEffect<E extends Enchantment.Effect>(
        @NotNull Enchantment.Target enchanted,
        @NotNull Enchantment.Target affected,
        @NotNull E effect,
        @Nullable DataPredicate requirements
) implements Enchantment.Effect {

    public static <E extends Enchantment.Effect> @NotNull BinaryTagSerializer<TargetedConditionalEffect<E>> nbtType(@NotNull BinaryTagSerializer<E> effectType) {
        return null; //todo
    }

}
