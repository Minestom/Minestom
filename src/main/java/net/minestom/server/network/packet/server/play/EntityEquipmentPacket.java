package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityEquipmentPacket(int entityId,
                                    @NotNull Map<EquipmentSlot, ItemStack> equipments) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public EntityEquipmentPacket {
        equipments = Map.copyOf(equipments);
        if (equipments.isEmpty())
            throw new IllegalArgumentException("Equipments cannot be empty");
    }

    public EntityEquipmentPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), readEquipments(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        int index = 0;
        for (var entry : equipments.entrySet()) {
            final boolean last = index++ == equipments.size() - 1;
            byte slotEnum = (byte) entry.getKey().ordinal();
            if (!last) slotEnum |= 0x80;
            writer.write(BYTE, slotEnum);
            writer.write(ItemStack.NETWORK_TYPE, entry.getValue());
        }
    }

    @Override
    public @NotNull Collection<Component> components() {
        return this.equipments.values()
                .stream()
                .map(item -> item.get(ItemComponent.CUSTOM_NAME))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        final var map = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        this.equipments.forEach((key, value) -> map.put(key, value.with(ItemComponent.CUSTOM_NAME, operator)));

        return new EntityEquipmentPacket(this.entityId, map);
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
