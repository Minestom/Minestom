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
    MAIN_HAND(0, 0, "mainhand", false, -1),
    OFF_HAND(1, 5, "offhand", false, -1),
    BOOTS(2, 1, "feet", true, BOOTS_SLOT),
    LEGGINGS(3, 2, "legs", true, LEGGINGS_SLOT),
    CHESTPLATE(4, 3, "chest", true, CHESTPLATE_SLOT),
    HELMET(5, 4, "head", true, HELMET_SLOT),
    BODY(6, 6, "body", false, -1);

    private static final List<EquipmentSlot> ARMORS = List.of(BOOTS, LEGGINGS, CHESTPLATE, HELMET);
    private static final Map<String, EquipmentSlot> BY_NBT_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlot::nbtName, slot -> slot));
    private static final Map<Integer, EquipmentSlot> BY_PROTOCOL_ID = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlot::protocolId, slot -> slot));
    private static final Map<Integer, EquipmentSlot> BY_LEGACY_PROTOCOL_ID = Arrays.stream(values())
            .collect(Collectors.toMap(EquipmentSlot::protocolId, slot -> slot));

    public static final NetworkBuffer.Type<EquipmentSlot> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(
            BY_PROTOCOL_ID::get, EquipmentSlot::protocolId);
    public static final BinaryTagSerializer<EquipmentSlot> NBT_TYPE = BinaryTagSerializer.STRING.map(
            BY_NBT_NAME::get, EquipmentSlot::nbtName);

    public static @NotNull List<@NotNull EquipmentSlot> armors() {
        return ARMORS;
    }

    @Deprecated
    public static @NotNull EquipmentSlot fromLegacyProtocolId(int legacyProtocolId) {
        final EquipmentSlot slot = BY_LEGACY_PROTOCOL_ID.get(legacyProtocolId);
        if (slot != null) return slot;

        throw new IllegalStateException("Unexpected value: " + legacyProtocolId);
    }

    private final int protocolId;
    private final int legacyProtocolId;
    private final String nbtName;
    private final boolean armor;
    private final int armorSlot;

    EquipmentSlot(int protocolId, int legacyProtocolId, @NotNull String nbtName, boolean armor, int armorSlot) {
        this.protocolId = protocolId;
        this.legacyProtocolId = legacyProtocolId;
        this.nbtName = nbtName;
        this.armor = armor;
        this.armorSlot = armorSlot;
    }

    public int protocolId() {
        return protocolId;
    }

    /**
     * Legacy protocol ID exists because that format is used in EntityEquipmentPacket
     * It is being referred to as the legacy ID here because newer components are using
     * the equipment slot stream codec (the more modern mechanism for network serialization)
     * The legacy ID is expected to be removed eventually.
     *
     * @return the equipment slot
     */
    @Deprecated
    public int legacyProtocolId() {
        return legacyProtocolId;
    }

    public @NotNull String nbtName() {
        return nbtName;
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
}
