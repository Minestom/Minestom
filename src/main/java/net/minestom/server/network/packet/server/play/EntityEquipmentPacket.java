package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public record EntityEquipmentPacket(int entityId, Map<EquipmentSlot, ItemStack> equipments) implements ServerPacket {
    public EntityEquipmentPacket {
        equipments = Map.copyOf(equipments);
    }

    public EntityEquipmentPacket(BinaryReader reader) {
        this(reader.readVarInt(), readEquipments(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);
        int index = 0;
        for (var entry : equipments.entrySet()) {
            final boolean last = index++ == equipments.size() - 1;
            byte slotEnum = (byte) entry.getKey().ordinal();
            if (!last) slotEnum |= 0x80;
            writer.writeByte(slotEnum);
            writer.writeItemStack(entry.getValue());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EQUIPMENT;
    }

    private static Map<EquipmentSlot, ItemStack> readEquipments(BinaryReader reader) {
        EnumMap<EquipmentSlot, ItemStack> equipments = new EnumMap<>(EquipmentSlot.class);
        boolean hasRemaining = true;
        while (hasRemaining) {
            byte slotEnum = reader.readByte();
            hasRemaining = (slotEnum & 0x80) == 0x80;
            equipments.put(EquipmentSlot.values()[slotEnum & 0x7F], reader.readItemStack());
        }
        return equipments;
    }
}
