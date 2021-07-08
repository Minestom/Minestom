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

import java.util.*;
import java.util.function.UnaryOperator;

public class EntityEquipmentPacket implements ComponentHoldingServerPacket {

    public int entityId;
    public EquipmentSlot[] slots;
    public ItemStack[] itemStacks;

    public EntityEquipmentPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(entityId);

        if (slots == null || itemStacks == null) {
            throw new IllegalArgumentException("You need to specify at least one slot and one item");
        }

        if (slots.length != itemStacks.length) {
            throw new IllegalArgumentException("You need the same amount of slots and items");
        }

        for (int i = 0; i < slots.length; i++) {
            final EquipmentSlot slot = slots[i];
            final ItemStack itemStack = itemStacks[i];
            final boolean last = i == slots.length - 1;

            byte slotEnum = (byte) slot.ordinal();
            if (!last) {
                slotEnum |= 0x80;
            }

            writer.writeByte(slotEnum);
            writer.writeItemStack(itemStack);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        entityId = reader.readVarInt();

        boolean hasRemaining = true;
        List<EquipmentSlot> slots = new LinkedList<>();
        List<ItemStack> stacks = new LinkedList<>();
        while (hasRemaining) {
            byte slotEnum = reader.readByte();
            hasRemaining = (slotEnum & 0x80) == 0x80;

            slots.add(EquipmentSlot.values()[slotEnum & 0x7F]);
            stacks.add(reader.readItemStack());
        }

        this.slots = slots.toArray(new EquipmentSlot[0]);
        this.itemStacks = stacks.toArray(new ItemStack[0]);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EQUIPMENT;
    }

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        for (ItemStack item : this.itemStacks) {
            components.addAll(item.components());
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        EntityEquipmentPacket packet = new EntityEquipmentPacket();
        packet.entityId = this.entityId;
        packet.slots = Arrays.copyOf(this.slots, this.slots.length);
        packet.itemStacks = new ItemStack[this.itemStacks.length];
        for (int i = 0; i < this.itemStacks.length; i++) {
            packet.itemStacks[i] = this.itemStacks[i].copyWithOperator(operator);
        }
        return packet;
    }
}
