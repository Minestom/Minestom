package net.minestom.server.item.attribute;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
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

    public static final NetworkBuffer.Type<AttributeSlot> NETWORK_TYPE = NetworkBuffer.Enum(AttributeSlot.class);
    public static final BinaryTagSerializer<AttributeSlot> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(AttributeSlot.class);

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

    /**
     * Returns true if this attribute slot has an effect on the given {@link EquipmentSlot}, false otherwise.
     */
    public boolean contains(@NotNull EquipmentSlot equipmentSlot) {
        return this.equipmentSlots.contains(equipmentSlot);
    }
}
