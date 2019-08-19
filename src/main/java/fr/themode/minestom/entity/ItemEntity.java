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
    }

    @Override
    public void update() {

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

    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }
}
