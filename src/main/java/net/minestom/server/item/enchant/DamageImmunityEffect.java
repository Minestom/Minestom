package net.minestom.server.item.enchant;

import net.minestom.server.utils.Unit;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public final class DamageImmunityEffect implements Enchantment.Effect {
    public static final DamageImmunityEffect INSTANCE = new DamageImmunityEffect();

    public static final @NotNull BinaryTagSerializer<DamageImmunityEffect> NBT_TYPE = BinaryTagSerializer.UNIT
            .map(ignored -> DamageImmunityEffect.INSTANCE, ignored -> Unit.INSTANCE);

    private DamageImmunityEffect() {}
}
