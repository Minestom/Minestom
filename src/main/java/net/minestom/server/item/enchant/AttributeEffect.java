package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;

public record AttributeEffect(
        @NotNull Key id,
        @NotNull Attribute attribute,
        @NotNull LevelBasedValue amount,
        @NotNull AttributeOperation operation
) implements Enchantment.Effect, LocationEffect {

    public static final Codec<AttributeEffect> CODEC = StructCodec.struct(
            "id", Codec.KEY, AttributeEffect::id,
            "attribute", Attribute.CODEC, AttributeEffect::attribute,
            "amount", LevelBasedValue.CODEC, AttributeEffect::amount,
            "operation", AttributeOperation.CODEC, AttributeEffect::operation,
            AttributeEffect::new);

    @Override
    public @NotNull Codec<AttributeEffect> codec() {
        return CODEC;
    }
}
