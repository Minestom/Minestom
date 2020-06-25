package net.minestom.server.network.packet.server.play;

import net.minestom.server.event.item.ArmorEquipEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class EntityEquipmentPacket implements ServerPacket {

    public int entityId;
    public Slot slot;
    public ItemStack itemStack;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        //TODO make better
        writer.writeByte((byte) slot.ordinal());
        writer.writeItemStack(itemStack);
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
