package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum EquipmentSlotGroup implements Predicate<EquipmentSlot> {
    ANY("any", EquipmentSlot.values()),
    MAIN_HAND("mainhand", EquipmentSlot.MAIN_HAND),
    OFF_HAND("offhand", EquipmentSlot.OFF_HAND),
    HAND("hand", EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND),
    FEET("feet", EquipmentSlot.BOOTS),
    LEGS("legs", EquipmentSlot.LEGGINGS),
    CHEST("chest", EquipmentSlot.CHESTPLATE),
    HEAD("head", EquipmentSlot.HELMET),
    ARMOR("armor", EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS, EquipmentSlot.HELMET),
    BODY("body", EquipmentSlot.BODY);

    private static final Map<String, EquipmentSlotGroup> BY_NBT_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlotGroup::nbtName, Function.identity()));

    public static final NetworkBuffer.Type<EquipmentSlotGroup> NETWORK_TYPE = NetworkBuffer.Enum(EquipmentSlotGroup.class);
    public static final BinaryTagSerializer<EquipmentSlotGroup> NBT_TYPE = BinaryTagSerializer.STRING
            .map(BY_NBT_NAME::get, EquipmentSlotGroup::nbtName);

    private final String nbtName;
    private final List<EquipmentSlot> equipmentSlots;

    EquipmentSlotGroup(@NotNull String nbtName, @NotNull EquipmentSlot... equipmentSlots) {
        this.equipmentSlots = List.of(equipmentSlots);
        this.nbtName = nbtName;
    }

    /**
     * Returns the (potentially multiple) equipment slots associated with this attribute slot.
     */
    public @NotNull List<EquipmentSlot> equipmentSlots() {
        return this.equipmentSlots;
    }

    public @NotNull String nbtName() {
        return this.nbtName;
    }

    /**
     * Returns true if this attribute slot has an effect on the given {@link EquipmentSlot}, false otherwise.
     */
    public boolean contains(@NotNull EquipmentSlot equipmentSlot) {
        return this.equipmentSlots.contains(equipmentSlot);
    }

    @Override
    public boolean test(EquipmentSlot equipmentSlot) {
        return this.contains(equipmentSlot);
    }
}
