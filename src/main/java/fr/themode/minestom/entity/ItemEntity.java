package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.EntityRelativeMovePacket;
import fr.themode.minestom.utils.Utils;

public class ItemEntity extends ObjectEntity {

    private ItemStack itemStack;
    private boolean pickable = true;

    public ItemEntity(ItemStack itemStack) {
        super(34);
        this.itemStack = itemStack;
    }

    @Override
    public void update() {
        // TODO how to keep items at the same position?
        EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
        entityRelativeMovePacket.entityId = getEntityId();
        entityRelativeMovePacket.onGround = false;
        sendPacketToViewers(entityRelativeMovePacket);
    }

    @Override
    public Buffer getMetadataBuffer() {
        Buffer buffer = super.getMetadataBuffer();
        buffer.putByte((byte) 7);
        buffer.putByte(METADATA_SLOT);
        Utils.writeItemStack(buffer, itemStack);
        return buffer;
    }

    @Override
    public int getData() {
        return 1;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        sendMetadataIndex(7); // Refresh itemstack for viewers
    }

    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }
}
