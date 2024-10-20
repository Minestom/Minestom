package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public enum EquipmentSlot {
    MAIN_HAND(false, -1, "mainhand"),
    OFF_HAND(false, -1, "offhand"),
    BOOTS(true, BOOTS_SLOT, "feet"),
    LEGGINGS(true, LEGGINGS_SLOT, "legs"),
    CHESTPLATE(true, CHESTPLATE_SLOT, "chest"),
    HELMET(true, HELMET_SLOT, "head"),
    BODY(false, -1, "body");

    private static final List<EquipmentSlot> ARMORS = List.of(BOOTS, LEGGINGS, CHESTPLATE, HELMET);
    private static final Map<String, EquipmentSlot> BY_NBT_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlot::nbtName, slot -> slot));

    public static final NetworkBuffer.Type<EquipmentSlot> NETWORK_TYPE = NetworkBuffer.Enum(EquipmentSlot.class);
    public static final BinaryTagSerializer<EquipmentSlot> NBT_TYPE = BinaryTagSerializer.STRING.map(
            BY_NBT_NAME::get, EquipmentSlot::nbtName);

    private final boolean armor;
    private final int armorSlot;
    private final String nbtName;

    EquipmentSlot(boolean armor, int armorSlot, String nbtName) {
        this.armor = armor;
        this.armorSlot = armorSlot;
        this.nbtName = nbtName;
    }

    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }

    public boolean isArmor() {
        return armor;
    }

    public int armorSlot() {
        return armorSlot;
    }

    public @NotNull String nbtName() {
        return nbtName;
    }

    public static @NotNull List<@NotNull EquipmentSlot> armors() {
        return ARMORS;
    }

}
