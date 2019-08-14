package fr.themode.minestom.item;

public class ItemStack {

    public static final ItemStack AIR_ITEM = new ItemStack(0, (byte) 1);

    private int itemId;
    private byte amount;

    public ItemStack(int itemId, byte amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    public boolean isAir() {
        return itemId == 0;
    }

    public boolean isSimilar(ItemStack itemStack) {
        return itemStack.getItemId() == itemId;
    }

    public byte getAmount() {
        return amount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public ItemStack clone() {
        return new ItemStack(itemId, amount);
    }
}
