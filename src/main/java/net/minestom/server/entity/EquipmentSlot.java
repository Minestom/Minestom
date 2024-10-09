package net.minestom.server.entity;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

public enum EquipmentSlot {
    MAIN_HAND(false, -1),
    OFF_HAND(false, -1),
    BOOTS(true, BOOTS_SLOT),
    LEGGINGS(true, LEGGINGS_SLOT),
    CHESTPLATE(true, CHESTPLATE_SLOT),
    HELMET(true, HELMET_SLOT),
    BODY(false, -1);

    private static final List<EquipmentSlot> ARMORS = List.of(BOOTS, LEGGINGS, CHESTPLATE, HELMET);

    public static final NetworkBuffer.Type<EquipmentSlot> NETWORK_TYPE = NetworkBuffer.Enum(EquipmentSlot.class);
    public static final BinaryTagSerializer<EquipmentSlot> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(EquipmentSlot.class);

    private final boolean armor;
    private final int armorSlot;

    EquipmentSlot(boolean armor, int armorSlot) {
        this.armor = armor;
        this.armorSlot = armorSlot;
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

    public static @NotNull List<@NotNull EquipmentSlot> armors() {
        return ARMORS;
    }

}
