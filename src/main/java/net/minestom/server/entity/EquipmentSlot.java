package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public enum EquipmentSlot {
    MAIN_HAND(false, -1, "mainhand", 0),
    BOOTS(true, BOOTS_SLOT, "feet", 2),
    LEGGINGS(true, LEGGINGS_SLOT, "legs", 3),
    CHESTPLATE(true, CHESTPLATE_SLOT, "chest", 4),
    HELMET(true, HELMET_SLOT, "head", 5),
    OFF_HAND(false, -1, "offhand", 1),
    BODY(false, -1, "body", 6);

    private static final List<EquipmentSlot> ARMORS = List.of(BOOTS, LEGGINGS, CHESTPLATE, HELMET);
    private static final Map<String, EquipmentSlot> BY_NBT_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlot::nbtName, slot -> slot));

    public static final NetworkBuffer.Type<EquipmentSlot> NETWORK_TYPE = NetworkBuffer.Enum(EquipmentSlot.class);
    public static final BinaryTagSerializer<EquipmentSlot> NBT_TYPE = BinaryTagSerializer.STRING.map(
            BY_NBT_NAME::get, EquipmentSlot::nbtName);

    private final boolean armor;
    private final int armorSlot;
    private final String nbtName;
    private final  int equipmentSlot;

    EquipmentSlot(boolean armor, int armorSlot, String nbtName, int equipmentSlot) {
        this.armor = armor;
        this.armorSlot = armorSlot;
        this.nbtName = nbtName;
        this.equipmentSlot = equipmentSlot;
    }

    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }

    public boolean isArmor() {
        return armor;
    }

    /**
     * Differs from the ordinal only slightly - the ordinal is used for sending components such as {@link Equippable},
     * but this value needs to be used instead for {@link EntityEquipmentPacket}.
     *
     * @return the equipment slot
     */
    public int equipmentSlot() {
        return equipmentSlot;
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

    @ApiStatus.Internal
    public static @NotNull EquipmentSlot fromEquipmentSlot(int equipmentSlot) {
        return switch (equipmentSlot) {
            case 0 -> EquipmentSlot.MAIN_HAND;
            case 1 -> EquipmentSlot.OFF_HAND;
            case 2 -> EquipmentSlot.BOOTS;
            case 3 -> EquipmentSlot.LEGGINGS;
            case 4 -> EquipmentSlot.CHESTPLATE;
            case 5 -> EquipmentSlot.HELMET;
            case 6 -> EquipmentSlot.BODY;
            default -> throw new IllegalStateException("Unexpected value: " + equipmentSlot);
        };
    }
}
