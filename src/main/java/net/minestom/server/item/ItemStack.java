package net.minestom.server.item;

import net.minestom.server.chat.ColoredText;
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

    private ColoredText displayName;
    private boolean unbreakable;
    private ArrayList<ColoredText> lore;

    private Map<Enchantment, Short> enchantmentMap;
    private Map<Enchantment, Short> storedEnchantmentMap;
    private List<ItemAttribute> attributes;
    private Set<PotionType> potionTypes;

    private int hideFlag;

    private StackingRule stackingRule;
    private Data data;

    {
        if (defaultStackingRule == null)
            defaultStackingRule = DEFAULT_STACKING_RULE;
        if (this.stackingRule == null)
            this.stackingRule = defaultStackingRule;
    }

    public ItemStack(short materialId, byte amount, short damage) {
        this.materialId = materialId;
        this.amount = amount;
        this.damage = damage;
        this.lore = new ArrayList<>();

        this.enchantmentMap = new HashMap<>();
        this.storedEnchantmentMap = new HashMap<>();
        this.attributes = new ArrayList<>();
        this.potionTypes = new HashSet<>();
    }

    public ItemStack(short materialId, byte amount) {
        this(materialId, amount, (short) 0);
    }

    public ItemStack(Material material, byte amount) {
        this(material.getId(), amount);
    }

    /**
     * Get the default stacking rule for newly created ItemStack
     *
     * @return the default stacking rule
     */
    public static StackingRule getDefaultStackingRule() {
        return defaultStackingRule;
    }

    /**
     * Change the default stacking rule for created item stack
     *
     * @param defaultStackingRule the default item stack
     * @throws NullPointerException if {@code defaultStackingRule} is null
     */
    public static void setDefaultStackingRule(StackingRule defaultStackingRule) {
        Check.notNull(defaultStackingRule, "StackingRule cannot be null!");
        ItemStack.defaultStackingRule = defaultStackingRule;
    }

    /**
     * Get if the item is air
     *
     * @return true if the material is air, false otherwise
     */
    public boolean isAir() {
        return materialId == Material.AIR.getId();
    }

    /**
     * Get if two items are similar.
     * It does not take {@link #getAmount()} and {@link #getStackingRule()} in consideration
     *
     * @param itemStack The ItemStack to compare to
     * @return true if both items are similar
     */
    public synchronized boolean isSimilar(ItemStack itemStack) {
        synchronized (itemStack) {
            final ColoredText itemDisplayName = itemStack.getDisplayName();
            final boolean displayNameCheck = (displayName == null && itemDisplayName == null) ||
                    (displayName != null && itemDisplayName != null && displayName.equals(itemDisplayName));

            final Data itemData = itemStack.getData();
            final boolean dataCheck = (data == null && itemData == null) ||
                    (data != null && itemData != null && data.equals(itemData));

            return itemStack.getMaterialId() == materialId &&
                    displayNameCheck &&
                    itemStack.isUnbreakable() == unbreakable &&
                    itemStack.getDamage() == damage &&
                    itemStack.enchantmentMap.equals(enchantmentMap) &&
                    itemStack.storedEnchantmentMap.equals(storedEnchantmentMap) &&
                    itemStack.attributes.equals(attributes) &&
                    itemStack.potionTypes.equals(potionTypes) &&
                    itemStack.hideFlag == hideFlag &&
                    dataCheck;
        }
    }

    public short getDamage() {
        return damage;
    }

    /**
     * Get the item amount
     * <p>
     * WARNING: for amount computation it would be better to use {@link StackingRule#getAmount(ItemStack)}
     * to support all stacking implementation
     *
     * @return the item amount
     */
    public byte getAmount() {
        return amount;
    }

    /**
     * Change the item amount
     * <p>
     * WARNING: for amount computation it would be better to use {@link StackingRule#getAmount(ItemStack)}
     * to support all stacking implementation
     *
     * @param amount the new item amount
     */
    public void setAmount(byte amount) {
        this.amount = amount;
    }

    public void setDamage(short damage) {
        this.damage = damage;
    }

    /**
     * Get the item internal material id
     *
     * @return the item material id
     */
    public short getMaterialId() {
        return materialId;
    }

    /**
     * Get the item material
     *
     * @return the item material
     */
    public Material getMaterial() {
        return Material.fromId(getMaterialId());
    }

    /**
     * Get the item display name
     *
     * @return the item display name, can be null if not present
     */
    public ColoredText getDisplayName() {
        return displayName;
    }

    /**
     * Set the item display name
     *
     * @param displayName the item display name
     */
    public void setDisplayName(ColoredText displayName) {
        this.displayName = displayName;
    }

    /**
     * Get if the item has a display name
     *
     * @return the item display name
     */
    public boolean hasDisplayName() {
        return displayName != null;
    }

    /**
     * Get the item lore
     *
     * @return the item lore, can be null if not present
     */
    public ArrayList<ColoredText> getLore() {
        return lore;
    }

    /**
     * Set the item lore
     *
     * @param lore the item lore, can be null to remove
     */
    public void setLore(ArrayList<ColoredText> lore) {
        this.lore = lore;
    }

    /**
     * Get if the item has a lore
     *
     * @return true if the item has lore, false otherwise
     */
    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    /**
     * Get the item enchantment map
     *
     * @return an unmodifiable map containing the item enchantments
     */
    public Map<Enchantment, Short> getEnchantmentMap() {
        return Collections.unmodifiableMap(enchantmentMap);
    }

    /**
     * Set an enchantment level
     *
     * @param enchantment the enchantment type
     * @param level       the enchantment level
     */
    public void setEnchantment(Enchantment enchantment, short level) {
        if (level < 1) {
            removeEnchantment(enchantment);
            return;
        }

        this.enchantmentMap.put(enchantment, level);
    }

    /**
     * Remove an enchantment
     *
     * @param enchantment the enchantment type
     */
    public void removeEnchantment(Enchantment enchantment) {
        this.enchantmentMap.remove(enchantment);
    }

    /**
     * Get an enchantment level
     *
     * @param enchantment the enchantment type
     * @return the stored enchantment level, 0 if not present
     */
    public int getEnchantmentLevel(Enchantment enchantment) {
        return this.enchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    /**
     * Get the stored enchantment map
     * Stored enchantments are used on enchanted book
     *
     * @return an unmodifiable map containing the item stored enchantments
     */
    public Map<Enchantment, Short> getStoredEnchantmentMap() {
        return Collections.unmodifiableMap(storedEnchantmentMap);
    }

    /**
     * Set a stored enchantment level
     *
     * @param enchantment the enchantment type
     * @param level       the enchantment level
     */
    public void setStoredEnchantment(Enchantment enchantment, short level) {
        if (level < 1) {
            removeStoredEnchantment(enchantment);
            return;
        }

        this.storedEnchantmentMap.put(enchantment, level);
    }

    /**
     * Remove a stored enchantment
     *
     * @param enchantment the enchantment type
     */
    public void removeStoredEnchantment(Enchantment enchantment) {
        this.storedEnchantmentMap.remove(enchantment);
    }

    /**
     * Get a stored enchantment level
     *
     * @param enchantment the enchantment type
     * @return the stored enchantment level, 0 if not present
     */
    public int getStoredEnchantmentLevel(Enchantment enchantment) {
        return this.storedEnchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    /**
     * Get the item attributes
     *
     * @return an unmodifiable {@link List} containing the item attributes
     */
    public List<ItemAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Add an attribute to the item
     *
     * @param itemAttribute the attribute to add
     */
    public void addAttribute(ItemAttribute itemAttribute) {
        this.attributes.add(itemAttribute);
    }

    /**
     * Remove an attribute to the item
     *
     * @param itemAttribute the attribute to remove
     */
    public void removeAttribute(ItemAttribute itemAttribute) {
        this.attributes.remove(itemAttribute);
    }

    /**
     * Get the item potion types
     *
     * @return an unmodifiable {@link Set} containing the item potion types
     */
    public Set<PotionType> getPotionTypes() {
        return Collections.unmodifiableSet(potionTypes);
    }

    /**
     * Add a potion type to the item
     *
     * @param potionType the potion type to add
     */
    public void addPotionType(PotionType potionType) {
        this.potionTypes.add(potionType);
    }

    /**
     * Remove a potion type to the item
     *
     * @param potionType the potion type to remove
     */
    public void removePotionType(PotionType potionType) {
        this.potionTypes.remove(potionType);
    }

    /**
     * Get the item hide flag
     *
     * @return the item hide flag
     */
    public int getHideFlag() {
        return hideFlag;
    }

    /**
     * Change the item hide flag. This is the integer sent when updating the item hide flag
     *
     * @param hideFlag the new item hide flag
     */
    public void setHideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
    }

    /**
     * Add flags to the item
     *
     * @param flags the flags to add
     */
    public void addItemFlags(ItemFlag... flags) {
        for (ItemFlag f : flags) {
            this.hideFlag |= getBitModifier(f);
        }
    }

    /**
     * Remove flags from the item
     *
     * @param flags the flags to remove
     */
    public void removeItemFlags(ItemFlag... flags) {
        for (ItemFlag f : flags) {
            this.hideFlag &= ~getBitModifier(f);
        }
    }

    /**
     * Get the item flags
     *
     * @return an unmodifiable {@link Set} containing the item flags
     */
    public Set<ItemFlag> getItemFlags() {
        Set<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);

        for (ItemFlag f : ItemFlag.values()) {
            if (hasItemFlag(f)) {
                currentFlags.add(f);
            }
        }

        return Collections.unmodifiableSet(currentFlags);
    }

    /**
     * Get if the item has an item flag
     *
     * @param flag the item flag
     * @return true if the item has the flag {@code flag}, false otherwise
     */
    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    /**
     * Get if the item is unbreakable
     *
     * @return true if the item is unbreakable, false otherwise
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * Make the item unbreakable
     *
     * @param unbreakable true to make the item unbreakable, false otherwise
     */
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    /**
     * Get if the item has any nbt tag
     *
     * @return true if the item has nbt tag, false otherwise
     */
    public boolean hasNbtTag() {
        return hasDisplayName() || hasLore() || isUnbreakable() ||
                !enchantmentMap.isEmpty() || !storedEnchantmentMap.isEmpty() ||
                !attributes.isEmpty() || !potionTypes.isEmpty();
    }

    /**
     * Clone this item stack
     *
     * @return a cloned item stack
     */
    public synchronized ItemStack clone() {
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

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    /**
     * Get the item stacking rule
     *
     * @return the item stacking rule
     */
    public StackingRule getStackingRule() {
        return stackingRule;
    }

    /**
     * Change the stacking rule of the item
     *
     * @param stackingRule the new item stacking rule
     * @throws NullPointerException if {@code stackingRule} is null
     */
    public void setStackingRule(StackingRule stackingRule) {
        Check.notNull(stackingRule, "StackingRule cannot be null!");
        this.stackingRule = stackingRule;
    }

    private byte getBitModifier(ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }
}
