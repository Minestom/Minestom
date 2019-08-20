package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityEquipmentPacket implements ServerPacket {

    public int entityId;
    public Slot slot;
    public ItemStack itemStack;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        Utils.writeVarInt(buffer, slot.ordinal());
        Utils.writeItemStack(buffer, itemStack);
    }

    @Override
    public int getId() {
        return 0x46;
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
