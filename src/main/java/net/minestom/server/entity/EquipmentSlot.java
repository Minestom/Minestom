package net.minestom.server.entity;

import net.minestom.server.item.attribute.AttributeSlot;
import org.jetbrains.annotations.NotNull;

public enum EquipmentSlot {
    MAIN_HAND,
    OFF_HAND,
    BOOTS,
    LEGGINGS,
    CHESTPLATE,
    HELMET;

    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }

    public boolean isArmor() {
        return !isHand();
    }

    public static EquipmentSlot fromAttributeSlot(@NotNull AttributeSlot attributeSlot) {
        switch (attributeSlot) {
            case MAINHAND:
                return MAIN_HAND;
            case OFFHAND:
                return OFF_HAND;
            case FEET:
                return BOOTS;
            case LEGS:
                return LEGGINGS;
            case CHEST:
                return CHESTPLATE;
            case HEAD:
                return HELMET;
        }
        throw new IllegalStateException("Something weird happened");
    }
}
