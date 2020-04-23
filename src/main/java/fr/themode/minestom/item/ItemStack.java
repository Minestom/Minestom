package fr.themode.minestom.item;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.data.DataContainer;
import fr.themode.minestom.item.rule.VanillaStackingRule;

import java.util.ArrayList;

public class ItemStack implements DataContainer {

    public static ItemStack getAirItem() {
        return new ItemStack((short) 0, (byte) 0);
    }

    private static StackingRule defaultStackingRule;

    private short materialId;

    {
        if (defaultStackingRule == null)
            defaultStackingRule = new VanillaStackingRule(127);
    }

    private byte amount;
    private short damage;

    private String displayName;
    private boolean unbreakable;
    private ArrayList<String> lore;

    private StackingRule stackingRule;
    private Data data;

    public ItemStack(short materialId, byte amount, short damage) {
        this.materialId = materialId;
        this.amount = amount;
        this.damage = damage;
        this.lore = new ArrayList<>();

        this.stackingRule = defaultStackingRule;
    }

    public ItemStack(short materialId, byte amount) {
        this(materialId, amount, (short) 0);
    }

    public ItemStack(Material material, byte amount) {
        this(material.getId(), amount);
    }

    public boolean isAir() {
        return materialId == Material.AIR.getId();
    }

    /**
     * Do not take amount in consideration
     *
     * @param itemStack The ItemStack to compare to
     * @return true if both items are similar (without comparing amount)
     */
    public boolean isSimilar(ItemStack itemStack) {
        return itemStack.getMaterialId() == materialId &&
                itemStack.getDisplayName() == displayName &&
                itemStack.isUnbreakable() == unbreakable &&
                itemStack.getDamage() == damage &&
                itemStack.getStackingRule() == stackingRule &&
                itemStack.getData() == data;
    }

    public byte getAmount() {
        return amount;
    }

    public short getDamage() {
        return damage;
    }

    public short getMaterialId() {
        return materialId;
    }

    public Material getMaterial() {
        return Material.fromId(getMaterialId());
    }

    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    public void setLore(ArrayList<String> lore) {
        this.lore = lore;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean hasNbtTag() {
        return hasDisplayName() || hasLore() || isUnbreakable();
    }

    public ItemStack clone() {
        ItemStack itemStack = new ItemStack(materialId, amount, damage);
        itemStack.setDisplayName(displayName);
        itemStack.setUnbreakable(unbreakable);
        itemStack.setLore(getLore());
        itemStack.setStackingRule(getStackingRule());
        Data data = getData();
        if (data != null)
            itemStack.setData(data.clone());
        return itemStack;
    }

    public StackingRule getStackingRule() {
        return stackingRule;
    }

    public void setStackingRule(StackingRule stackingRule) {
        if (stackingRule == null)
            throw new NullPointerException("StackingRule cannot be null!");

        this.stackingRule = stackingRule;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    public static StackingRule getDefaultStackingRule() {
        return defaultStackingRule;
    }

    public static void setDefaultStackingRule(StackingRule defaultStackingRule) {
        if (defaultStackingRule == null)
            throw new NullPointerException("StackingRule cannot be null!");

        ItemStack.defaultStackingRule = defaultStackingRule;
    }
}
