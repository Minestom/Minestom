package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;

public record AttributeEffect(
        Key id,
        Attribute attribute,
        LevelBasedValue amount,
        AttributeOperation operation
) implements Enchantment.Effect, LocationEffect {

    public static final StructCodec<AttributeEffect> CODEC = StructCodec.struct(
            "id", Codec.KEY, AttributeEffect::id,
            "attribute", Attribute.CODEC, AttributeEffect::attribute,
            "amount", LevelBasedValue.CODEC, AttributeEffect::amount,
            "operation", AttributeOperation.CODEC, AttributeEffect::operation,
            AttributeEffect::new);

    @Override
    public StructCodec<AttributeEffect> codec() {
        return CODEC;
    }
}
