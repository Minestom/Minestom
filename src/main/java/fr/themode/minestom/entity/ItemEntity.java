package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.utils.Utils;

public class ItemEntity extends ObjectEntity {

    private ItemStack itemStack;
    private boolean pickable = true;

    public ItemEntity(ItemStack itemStack) {
        super(34);
        this.itemStack = itemStack;
        setGravity(0.02f);
    }

    @Override
    public void update() {

    }

    @Override
    public void spawn() {
        // setVelocity(new Vector(0, 1, 0), 5000);
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
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
    public int getObjectData() {
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
