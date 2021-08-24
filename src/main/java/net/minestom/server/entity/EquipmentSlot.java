package net.minestom.server.entity;

import net.minestom.server.item.attribute.AttributeSlot;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public enum EquipmentSlot {
    MAIN_HAND(false, -1),
    OFF_HAND(false, -1),
    BOOTS(true, BOOTS_SLOT),
    LEGGINGS(true, LEGGINGS_SLOT),
    CHESTPLATE(true, CHESTPLATE_SLOT),
    HELMET(true, HELMET_SLOT);

    private final boolean armor;
    private final int armorSlot;

    EquipmentSlot(boolean armor, int armorSlot) {
        this.armor = armor;
        this.armorSlot = armorSlot;
    }

    public boolean isHand() {
        return !armor;
    }

    public boolean isArmor() {
        return armor;
    }

    public int armorSlot() {
        return armorSlot;
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
