package net.minestom.server.item.enchant;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public final class DamageImmunityEffect implements Enchantment.Effect {
    public static final DamageImmunityEffect INSTANCE = new DamageImmunityEffect();

    public static final Codec<DamageImmunityEffect> CODEC = StructCodec.struct(INSTANCE);

    private DamageImmunityEffect() {
    }
}
