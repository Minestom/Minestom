package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class EntityEquipmentPacket implements ServerPacket {

    public int entityId;
    public Slot slot;
    public ItemStack itemStack;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(entityId);
        writer.writeVarInt(slot.ordinal());
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
    }

}
