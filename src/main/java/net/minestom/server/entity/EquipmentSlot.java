package net.minestom.server.entity;

import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum EquipmentSlot {
    MAIN_HAND(false, -1),
    OFF_HAND(false, -1),
    BOOTS(true, PlayerInventoryUtils.BOOTS_SLOT),
    LEGGINGS(true, PlayerInventoryUtils.LEGGINGS_SLOT),
    CHESTPLATE(true, PlayerInventoryUtils.CHESTPLATE_SLOT),
    HELMET(true, PlayerInventoryUtils.HELMET_SLOT);

    private static final List<EquipmentSlot> ARMORS = List.of(BOOTS, LEGGINGS, CHESTPLATE, HELMET);

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

    public static @NotNull List<@NotNull EquipmentSlot> armors() {
        return ARMORS;
    }

    public static @NotNull EquipmentSlot fromAttributeSlot(@NotNull AttributeSlot attributeSlot) {
        return switch (attributeSlot) {
            case MAINHAND -> MAIN_HAND;
            case OFFHAND -> OFF_HAND;
            case FEET -> BOOTS;
            case LEGS -> LEGGINGS;
            case CHEST -> CHESTPLATE;
            case HEAD -> HELMET;
        };
    }
}
