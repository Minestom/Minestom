package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record AttributeEffect(
        @NotNull Key id,
        @NotNull Attribute attribute,
        @NotNull LevelBasedValue amount,
        @NotNull AttributeOperation operation
) implements Enchantment.Effect, LocationEffect {

    public static final BinaryTagSerializer<AttributeEffect> NBT_TYPE = BinaryTagSerializer.object(
            "id", BinaryTagSerializer.STRING.map(Key::key, Key::asString), AttributeEffect::id,
            "attribute", Attribute.NBT_TYPE, AttributeEffect::attribute,
            "amount", LevelBasedValue.NBT_TYPE, AttributeEffect::amount,
            "operation", AttributeOperation.NBT_TYPE, AttributeEffect::operation,
            AttributeEffect::new
    );

    @Override
    public @NotNull BinaryTagSerializer<AttributeEffect> nbtType() {
        return NBT_TYPE;
    }
}
