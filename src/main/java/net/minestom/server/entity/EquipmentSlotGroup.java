package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum EquipmentSlotGroup {
    ANY(EquipmentSlot.values()),
    MAIN_HAND(EquipmentSlot.MAIN_HAND),
    OFF_HAND(EquipmentSlot.OFF_HAND),
    HAND(EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND),
    FEET(EquipmentSlot.BOOTS),
    LEGS(EquipmentSlot.LEGGINGS),
    CHEST(EquipmentSlot.CHESTPLATE),
    HEAD(EquipmentSlot.HELMET),
    ARMOR(EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS, EquipmentSlot.HELMET),
    BODY(EquipmentSlot.BODY);

    public static final NetworkBuffer.Type<EquipmentSlotGroup> NETWORK_TYPE = NetworkBuffer.Enum(EquipmentSlotGroup.class);
    public static final BinaryTagSerializer<EquipmentSlotGroup> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(EquipmentSlotGroup.class);

    private final List<EquipmentSlot> equipmentSlots;

    EquipmentSlotGroup(@NotNull EquipmentSlot... equipmentSlots) {
        this.equipmentSlots = List.of(equipmentSlots);
    }

    /**
     * Returns the (potentially multiple) equipment slots associated with this attribute slot.
     */
    public @NotNull List<EquipmentSlot> equipmentSlots() {
        return this.equipmentSlots;
    }

    /**
     * Returns true if this attribute slot has an effect on the given {@link EquipmentSlot}, false otherwise.
     */
    public boolean contains(@NotNull EquipmentSlot equipmentSlot) {
        return this.equipmentSlots.contains(equipmentSlot);
    }
}
