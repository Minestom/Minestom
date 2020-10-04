package net.minestom.server.item;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.metadata.*;
import net.minestom.server.item.rule.VanillaStackingRule;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.validate.Check;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;

// TODO should we cache a ByteBuf of this item for faster packet write
public class ItemStack implements DataContainer {

    private static final StackingRule DEFAULT_STACKING_RULE = new VanillaStackingRule(127);

    private final Material material;

    private static StackingRule defaultStackingRule;
    private ItemMeta itemMeta;

    private byte amount;
    private int damage;

    public ItemStack(Material material, byte amount, int damage) {
        this.material = material;
        this.amount = amount;
        this.damage = damage;
        this.lore = new ArrayList<>();

        this.enchantmentMap = new HashMap<>();
        this.attributes = new ArrayList<>();

        this.itemMeta = findMeta();
    }

    private ColoredText displayName;
    private boolean unbreakable;
    private ArrayList<ColoredText> lore;

    private Map<Enchantment, Short> enchantmentMap;
    private List<ItemAttribute> attributes;

    private int hideFlag;
    private int customModelData;

    private StackingRule stackingRule;
    private Data data;

    private NBTConsumer nbtConsumer;

    {
        if (defaultStackingRule == null)
            defaultStackingRule = DEFAULT_STACKING_RULE;
        this.stackingRule = defaultStackingRule;
    }

    public ItemStack(Material material, byte amount) {
        this(material, amount, (short) 0);
    }

    /**
     * Get a new air item
     *
     * @return an air item
     */
    public static ItemStack getAirItem() {
        return new ItemStack(Material.AIR, (byte) 0);
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

    public static ItemStack fromNBT(NBTCompound nbt) {
        if (!nbt.containsKey("id") || !nbt.containsKey("Count"))
            throw new IllegalArgumentException("Invalid item NBT, must at least contain 'id' and 'Count' tags");
        final Material material = Registries.getMaterial(nbt.getString("id"));
        final byte count = nbt.getByte("Count");

        ItemStack s = new ItemStack(material, count);

        NBTCompound tag = nbt.getCompound("tag");
        if (tag != null) {
            NBTUtils.loadDataIntoItem(s, tag);
        }
        return s;
    }

    /**
     * Get if the item is air
     *
     * @return true if the material is air, false otherwise
     */
    public boolean isAir() {
        return material == Material.AIR;
    }

    /**
     * Get if two items are similar.
     * It does not take {@link #getAmount()} and {@link #getStackingRule()} in consideration
     *
     * @param itemStack The ItemStack to compare to
     * @return true if both items are similar
     */
    public boolean isSimilar(ItemStack itemStack) {
        synchronized (ItemStack.class) {
            final ColoredText itemDisplayName = itemStack.getDisplayName();
            final boolean displayNameCheck = (displayName == null && itemDisplayName == null) ||
                    (displayName != null && itemDisplayName != null && displayName.equals(itemDisplayName));

            final Data itemData = itemStack.getData();
            final boolean dataCheck = (data == null && itemData == null) ||
                    (data != null && itemData != null && data.equals(itemData));

            final boolean sameMeta = (itemStack.itemMeta == null && itemMeta == null) ||
                    (itemStack.itemMeta != null && itemMeta != null && (itemStack.itemMeta.isSimilar(itemMeta)));

            return itemStack.getMaterial() == material &&
                    displayNameCheck &&
                    itemStack.isUnbreakable() == unbreakable &&
                    itemStack.getDamage() == damage &&
                    itemStack.enchantmentMap.equals(enchantmentMap) &&
                    itemStack.attributes.equals(attributes) &&
                    itemStack.hideFlag == hideFlag &&
                    sameMeta &&
                    dataCheck;
        }
    }

    /**
     * Get the item damage (durability)
     *
     * @return the item damagel
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Set the item damage (durability)
     *
     * @param damage the item damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
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

    /**
     * Get the special meta object for this item
     * <p>
     * Can be null if not any
     *
     * @return the item meta
     */
    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    /**
     * Change the item meta linked to this item
     * <p>
     * WARNING: be sure to have nbt data useful for this item, items should automatically get the appropriate
     * item meta
     *
     * @param itemMeta the new item meta
     */
    public void setItemMeta(ItemMeta itemMeta) {
        this.itemMeta = itemMeta;
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
     * Get the item attributes
     *
     * @return an unmodifiable {@link List} containing the item attributes
     */
    public List<ItemAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Get the {@link ItemAttribute} with the specified internal name
     *
     * @param internalName the internal name of the attribute
     * @return the {@link ItemAttribute} with the internal name, null if not found
     */
    public ItemAttribute getAttribute(String internalName) {
        for (ItemAttribute itemAttribute : attributes) {
            if (itemAttribute.getInternalName().equals(internalName))
                return itemAttribute;
        }
        return null;
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
     * Get the item custom model data
     *
     * @return the item custom model data
     */
    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Change the item custom model data
     *
     * @param customModelData the new item custom data model
     */
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
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
        final int bitModifier = getBitModifier(flag);
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
     * Get the item material
     *
     * @return the item material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Get if the item has any nbt tag
     *
     * @return true if the item has nbt tag, false otherwise
     */
    public boolean hasNbtTag() {
        return hasDisplayName() ||
                hasLore() ||
                damage != 0 ||
                isUnbreakable() ||
                !enchantmentMap.isEmpty() ||
                !attributes.isEmpty() ||
                hideFlag != 0 ||
                customModelData != 0 ||
                (itemMeta != null && itemMeta.hasNbt());
    }

    /**
     * Clone this item stack
     *
     * @return a cloned item stack
     */
    public synchronized ItemStack clone() {
        ItemStack itemStack = new ItemStack(material, amount, damage);
        itemStack.setDisplayName(displayName);
        itemStack.setUnbreakable(unbreakable);
        itemStack.setLore(new ArrayList<>(getLore()));
        itemStack.setStackingRule(getStackingRule());

        itemStack.enchantmentMap = new HashMap<>(enchantmentMap);
        itemStack.attributes = new ArrayList<>(attributes);

        itemStack.hideFlag = hideFlag;
        itemStack.customModelData = customModelData;

        if (itemMeta != null)
            itemStack.itemMeta = itemMeta.clone();

        final Data data = getData();
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
     * Get the nbt consumer called when the item is serialized into a packet
     *
     * @return the item nbt consumer, null if not any
     */
    public NBTConsumer getNBTConsumer() {
        return nbtConsumer;
    }

    /**
     * Change the item nbt consumer
     *
     * @param nbtConsumer the new item nbt consumer
     */
    public void setNBTConsumer(NBTConsumer nbtConsumer) {
        this.nbtConsumer = nbtConsumer;
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
        Check.notNull(stackingRule, "The stacking rule cannot be null!");
        this.stackingRule = stackingRule;
    }

    /**
     * Consume this item by a specific amount
     * <p>
     * Will return null if the amount's amount isn't enough
     *
     * @param amount the quantity to consume
     * @return the new item with the updated amount, null if the item cannot be consumed by this much
     */
    public ItemStack consume(int amount) {
        final int currentAmount = stackingRule.getAmount(this);
        if (currentAmount < amount)
            return null;
        return stackingRule.apply(this, currentAmount - amount);
    }

    private byte getBitModifier(ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }

    /**
     * Find the item meta based on the material type
     *
     * @return the item meta
     */
    private ItemMeta findMeta() {
        if (material == Material.POTION ||
                material == Material.LINGERING_POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.TIPPED_ARROW)
            return new PotionMeta();

        if (material == Material.FILLED_MAP)
            return new MapMeta();

        if (material == Material.COMPASS)
            return new CompassMeta();

        if (material == Material.ENCHANTED_BOOK)
            return new EnchantedBookMeta();

        if (material == Material.CROSSBOW)
            return new CrossbowMeta();

        if (material == Material.WRITABLE_BOOK)
            return new WritableBookMeta();

        if (material == Material.WRITTEN_BOOK)
            return new WrittenBookMeta();

        if (material == Material.FIREWORK_ROCKET)
            return new FireworkMeta();

        if (material == Material.PLAYER_HEAD)
            return new PlayerHeadMeta();

        if (material == Material.LEATHER_HELMET ||
                material == Material.LEATHER_CHESTPLATE ||
                material == Material.LEATHER_LEGGINGS ||
                material == Material.LEATHER_BOOTS)
            return new LeatherArmorMeta();

        return null;
    }

    public NBTCompound toNBT() {
        NBTCompound compound = new NBTCompound()
                .setByte("Count", amount)
                .setString("id", material.getName());
        if (hasNbtTag()) {
            NBTCompound additionalTag = new NBTCompound();
            NBTUtils.saveDataIntoNBT(this, additionalTag);
            compound.set("tag", additionalTag);
        }
        return compound;
    }

    /**
     * WARNING: not implemented yet
     * <p>
     * This is be called each time an item is serialized to be send to a player,
     * can be used to customize the display of the item based on player data
     *
     * @param player the player
     * @return the custom {@link ItemDisplay} for {@code player},
     * null to use the normal item display name &amp; lore
     */
    public ItemDisplay getCustomDisplay(Player player) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Callback events

    /**
     * Called when the player right clicks with this item
     *
     * @param player the player who used the item
     * @param hand   the hand used
     */
    public void onRightClick(Player player, Player.Hand hand) {
    }

    /**
     * Called when the player left clicks with this item
     *
     * @param player the player who used the item
     * @param hand   the hand used
     */
    public void onLeftClick(Player player, Player.Hand hand) {
    }

    /**
     * Called when the player right clicks with this item on a block
     *
     * @param player    the player who used the item
     * @param hand      the hand used
     * @param position  the position of the interacted block
     * @param blockFace the block face
     * @return true if it prevents normal item use (placing blocks for instance)
     */
    public boolean onUseOnBlock(Player player, Player.Hand hand, BlockPosition position, Direction blockFace) {
        return false;
    }

    /**
     * Called when the player click on this item on an inventory
     * <p>
     * Executed before any events
     *
     * @param player          the player who clicked on the item
     * @param clickType       the click type
     * @param slot            the slot clicked
     * @param playerInventory true if the click is in the player inventory
     */
    public void onInventoryClick(Player player, ClickType clickType, int slot, boolean playerInventory) {

    }
}
