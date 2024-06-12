package net.minestom.server.item.enchant;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record AttributeEffect(
        @NotNull NamespaceID id,
        @NotNull Attribute attribute,
        @NotNull LevelBasedValue amount,
        @NotNull AttributeOperation operation
) implements Enchantment.Effect {

    public static final BinaryTagSerializer<AttributeEffect> NBT_TYPE = null; //todo
}
