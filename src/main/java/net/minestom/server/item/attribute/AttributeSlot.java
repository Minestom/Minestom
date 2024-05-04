package net.minestom.server.item.attribute;

import net.minestom.server.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum AttributeSlot {
    ANY(EquipmentSlot.values()),
    MAINHAND(EquipmentSlot.MAIN_HAND),
    OFFHAND(EquipmentSlot.OFF_HAND),
    FEET(EquipmentSlot.BOOTS),
    LEGS(EquipmentSlot.LEGGINGS),
    CHEST(EquipmentSlot.CHESTPLATE),
    HEAD(EquipmentSlot.HELMET),
    ARMOR(EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS, EquipmentSlot.HELMET),
    BODY(EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS);

    private final List<EquipmentSlot> equipmentSlots;

    AttributeSlot(@NotNull EquipmentSlot... equipmentSlots) {
        this.equipmentSlots = List.of(equipmentSlots);
    }

    /**
     * Returns the (potentially multiple) equipment slots associated with this attribute slot.
     */
    public @NotNull List<EquipmentSlot> equipmentSlots() {
        return this.equipmentSlots;
    }
}
