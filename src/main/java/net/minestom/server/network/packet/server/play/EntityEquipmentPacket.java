package net.minestom.server.network.packet.server.play;

import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class EntityEquipmentPacket implements ServerPacket {

    public int entityId;
    public Slot[] slots;
    public ItemStack[] itemStacks;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(entityId);

        if (slots == null || itemStacks == null) {
            throw new IllegalArgumentException("You need to specify at least one slot and one item");
        }

        if (slots.length != itemStacks.length) {
            throw new IllegalArgumentException("You need the same amount of slots and items");
        }

        for (int i = 0; i < slots.length; i++) {
            final Slot slot = slots[i];
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
    public int getId() {
        return ServerPacketIdentifier.ENTITY_EQUIPMENT;
    }

    public enum Slot {
        MAIN_HAND,
        OFF_HAND,
        BOOTS,
        LEGGINGS,
        CHESTPLATE,
        HELMET;

        public static Slot fromArmorSlot(ArmorEquipEvent.ArmorSlot armorSlot) {
            switch (armorSlot) {
                case HELMET:
                    return HELMET;
                case CHESTPLATE:
                    return CHESTPLATE;
                case LEGGINGS:
                    return LEGGINGS;
                case BOOTS:
                    return BOOTS;
                default:
                    return null;
            }
        }

    }

}
