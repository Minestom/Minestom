package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class EntityEquipmentPacket implements ServerPacket {

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

}
