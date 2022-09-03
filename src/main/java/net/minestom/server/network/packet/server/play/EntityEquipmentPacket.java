package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public record EntityEquipmentPacket(int entityId,
                                    @NotNull Map<EquipmentSlot, ItemStack> equipments) implements ComponentHoldingServerPacket {
    public EntityEquipmentPacket {
        equipments = Map.copyOf(equipments);
        if (equipments.isEmpty())
            throw new IllegalArgumentException("Equipments cannot be empty");
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

    @Override
    public @NotNull Collection<Component> components() {
        return this.equipments.values()
                .stream()
                .map(ItemStack::getDisplayName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var map = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        this.equipments.forEach((key, value) -> map.put(key, value.withDisplayName(operator)));

        return new EntityEquipmentPacket(this.entityId, map);
    }

    private static Map<EquipmentSlot, ItemStack> readEquipments(BinaryReader reader) {
        Map<EquipmentSlot, ItemStack> equipments = new EnumMap<>(EquipmentSlot.class);
        byte slot;
        do {
            slot = reader.readByte();
            equipments.put(EquipmentSlot.values()[slot & 0x7F], reader.readItemStack());
        } while ((slot & 0x80) == 0x80);
        return equipments;
    }
}
