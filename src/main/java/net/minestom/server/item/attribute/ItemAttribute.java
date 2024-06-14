package net.minestom.server.item.attribute;

import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ItemAttribute(
        @NotNull UUID uuid,
        @NotNull String name,
        @NotNull Attribute attribute,
        @NotNull AttributeOperation operation,
        double amount,
        @NotNull EquipmentSlotGroup slot
) {
}
