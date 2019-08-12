package fr.themode.minestom.item;

public class ItemStack {

    public static final ItemStack AIR_ITEM = new ItemStack(0, (byte) 1);

    private int itemId;
    private byte count;

    public ItemStack(int itemId, byte count) {
        this.itemId = itemId;
        this.count = count;
    }

    public int getItemId() {
        return itemId;
    }

    public byte getCount() {
        return count;
    }
}
