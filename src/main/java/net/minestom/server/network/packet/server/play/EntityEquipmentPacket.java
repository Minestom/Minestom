package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityEquipmentPacket(int entityId,
                                    @NotNull Map<EquipmentSlot, ItemStack> equipments) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public EntityEquipmentPacket {
        equipments = Map.copyOf(equipments);
        if (equipments.isEmpty())
            throw new IllegalArgumentException("Equipments cannot be empty");
    }

    public static final NetworkBuffer.Type<EntityEquipmentPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityEquipmentPacket value) {
            buffer.write(VAR_INT, value.entityId);
            int index = 0;
            for (var entry : value.equipments.entrySet()) {
                final boolean last = index++ == value.equipments.size() - 1;
                byte slotEnum = (byte) entry.getKey().ordinal();
                if (!last) slotEnum |= 0x80;
                buffer.write(BYTE, slotEnum);
                buffer.write(ItemStack.NETWORK_TYPE, entry.getValue());
            }
        }

        @Override
        public EntityEquipmentPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityEquipmentPacket(buffer.read(VAR_INT), readEquipments(buffer));
        }
    };

    @Override
    public @NotNull Collection<Component> components() {
        final var components = new ArrayList<Component>();
        for (var itemStack : this.equipments.values())
            components.addAll(ItemStack.textComponents(itemStack));
        return List.copyOf(components);
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var newEquipment = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        for (var entry : this.equipments.entrySet())
            newEquipment.put(entry.getKey(), ItemStack.copyWithOperator(entry.getValue(), operator));
        return new EntityEquipmentPacket(this.entityId, newEquipment);
    }

    private static Map<EquipmentSlot, ItemStack> readEquipments(@NotNull NetworkBuffer reader) {
        Map<EquipmentSlot, ItemStack> equipments = new EnumMap<>(EquipmentSlot.class);
        byte slot;
        do {
            slot = reader.read(BYTE);
            equipments.put(EquipmentSlot.values()[slot & 0x7F], reader.read(ItemStack.NETWORK_TYPE));
        } while ((slot & 0x80) == 0x80);
        return equipments;
    }
}
