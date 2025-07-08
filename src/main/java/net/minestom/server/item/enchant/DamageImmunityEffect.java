package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.utils.Unit;
import org.jetbrains.annotations.NotNull;

public final class DamageImmunityEffect implements Enchantment.Effect {
    public static final DamageImmunityEffect INSTANCE = new DamageImmunityEffect();

    public static final @NotNull Codec<DamageImmunityEffect> CODEC = Codec.UNIT
            .transform(ignored -> DamageImmunityEffect.INSTANCE, ignored -> Unit.INSTANCE);

    private DamageImmunityEffect() {
    }
}
