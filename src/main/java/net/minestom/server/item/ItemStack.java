package net.minestom.server.item;

import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.rule.VanillaStackingRule;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.validate.Check;

import java.util.*;

public class ItemStack implements DataContainer {

    private static final StackingRule DEFAULT_STACKING_RULE = new VanillaStackingRule(127);

    public static ItemStack getAirItem() {
        return new ItemStack((short) 0, (byte) 0);
    }

    private static StackingRule defaultStackingRule;

    private short materialId;

    private byte amount;
    private short damage;

    private String displayName;
    private boolean unbreakable;
    private ArrayList<String> lore;

    private Map<Enchantment, Short> enchantmentMap;
    private Map<Enchantment, Short> storedEnchantmentMap;
    private List<ItemAttribute> attributes;
    private Set<PotionType> potionTypes;

    {
        if (defaultStackingRule == null)
            defaultStackingRule = DEFAULT_STACKING_RULE;
    }

    private int hideFlag;

    private StackingRule stackingRule;
    private Data data;

    public ItemStack(short materialId, byte amount, short damage) {
        this.materialId = materialId;
        this.amount = amount;
        this.damage = damage;
        this.lore = new ArrayList<>();

        this.enchantmentMap = new HashMap<>();
        this.storedEnchantmentMap = new HashMap<>();
        this.attributes = new ArrayList<>();
        this.potionTypes = new HashSet<>();

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
                itemStack.enchantmentMap.equals(enchantmentMap) &&
                itemStack.hideFlag == hideFlag &&
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

    public Map<Enchantment, Short> getEnchantmentMap() {
        return Collections.unmodifiableMap(enchantmentMap);
    }

    public void setEnchantment(Enchantment enchantment, short level) {
        if (level < 1) {
            removeEnchantment(enchantment);
            return;
        }

        this.enchantmentMap.put(enchantment, level);
    }

    public void removeEnchantment(Enchantment enchantment) {
        this.enchantmentMap.remove(enchantment);
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return this.enchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    /**
     * Stored enchantments are used on enchanted book
     *
     * @return an unmodifiable map containing the stored enchantments
     */
    public Map<Enchantment, Short> getStoredEnchantmentMap() {
        return Collections.unmodifiableMap(storedEnchantmentMap);
    }

    public void setStoredEnchantment(Enchantment enchantment, short level) {
        if (level < 1) {
            removeStoredEnchantment(enchantment);
            return;
        }

        this.storedEnchantmentMap.put(enchantment, level);
    }

    public void removeStoredEnchantment(Enchantment enchantment) {
        this.storedEnchantmentMap.remove(enchantment);
    }

    public int getStoredEnchantmentLevel(Enchantment enchantment) {
        return this.storedEnchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    public List<ItemAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public void addAttribute(ItemAttribute itemAttribute) {
        this.attributes.add(itemAttribute);
    }

    public Set<PotionType> getPotionTypes() {
        return Collections.unmodifiableSet(potionTypes);
    }

    public void addPotionType(PotionType potionType) {
        this.potionTypes.add(potionType);
    }

    public int getHideFlag() {
        return hideFlag;
    }

    public void setHideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
    }

    public void addItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag |= getBitModifier(f);
        }
    }

    public void removeItemFlags(ItemFlag... hideFlags) {
        for (ItemFlag f : hideFlags) {
            this.hideFlag &= ~getBitModifier(f);
        }
    }

    public Set<ItemFlag> getItemFlags() {
        Set<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);

        for (ItemFlag f : ItemFlag.values()) {
            if (hasItemFlag(f)) {
                currentFlags.add(f);
            }
        }

        return currentFlags;
    }

    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean hasNbtTag() {
        return hasDisplayName() || hasLore() || isUnbreakable() ||
                !enchantmentMap.isEmpty() || !storedEnchantmentMap.isEmpty() ||
                !attributes.isEmpty() || !potionTypes.isEmpty();
    }

    public ItemStack clone() {
        ItemStack itemStack = new ItemStack(materialId, amount, damage);
        itemStack.setDisplayName(displayName);
        itemStack.setUnbreakable(unbreakable);
        itemStack.setLore(getLore());
        itemStack.setStackingRule(getStackingRule());

        itemStack.enchantmentMap = new HashMap<>(enchantmentMap);
        itemStack.storedEnchantmentMap = new HashMap<>(storedEnchantmentMap);
        itemStack.attributes = new ArrayList<>(attributes);
        itemStack.potionTypes = new HashSet<>(potionTypes);
        itemStack.hideFlag = hideFlag;

        Data data = getData();
        if (data != null)
            itemStack.setData(data.clone());
        return itemStack;
    }

    public StackingRule getStackingRule() {
        return stackingRule;
    }

    public static void setDefaultStackingRule(StackingRule defaultStackingRule) {
        Check.notNull(defaultStackingRule, "StackingRule cannot be null!");
        ItemStack.defaultStackingRule = defaultStackingRule;
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

    public void setStackingRule(StackingRule stackingRule) {
        Check.notNull(stackingRule, "StackingRule cannot be null!");
        this.stackingRule = stackingRule;
    }

    private byte getBitModifier(ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }
}
