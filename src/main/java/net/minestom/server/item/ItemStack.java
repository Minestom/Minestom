package net.minestom.server.item;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.metadata.*;
import net.minestom.server.item.rule.VanillaStackingRule;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import net.minestom.server.utils.ownership.OwnershipHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

// TODO should we cache a ByteBuf of this item for faster packet write

/**
 * Represents an item in an inventory ({@link PlayerInventory}, {@link Inventory}) or on the ground ({@link ItemEntity}).
 * <p>
 * An item stack cannot be null, you can however use {@link #getAirItem()} instead.
 * <p>
 * WARNING: all setters will not update the item automatically, it will need to be refreshed manually.
 * Here a non-exhaustive list of what you can do to update the item:
 * {@link PlayerInventory#refreshSlot(short)}, {@link Inventory#refreshSlot(short)} or a raw {@link SetSlotPacket}.
 */
public class ItemStack implements DataContainer, PublicCloneable<ItemStack>, HoverEventSource<ShowItem> {

    public static final OwnershipHandler<Data> DATA_OWNERSHIP = new OwnershipHandler<>();
    public static final String OWNERSHIP_DATA_KEY = "ownership_identifier";
    private static final StackingRule VANILLA_STACKING_RULE = new VanillaStackingRule(64);

    private final UUID identifier;

    private Material material;

    private static StackingRule defaultStackingRule;
    private ItemMeta itemMeta;

    private byte amount;
    private int damage;

    private Component displayName;
    private boolean unbreakable;
    private List<Component> lore;

    private Object2ShortMap<Enchantment> enchantmentMap;
    private List<ItemAttribute> attributes;

    private int hideFlag;
    private int customModelData;

    private StackingRule stackingRule;
    private Data data;

    private Set<String> canDestroy;
    private Set<String> canPlaceOn;

    {
        if (defaultStackingRule == null)
            defaultStackingRule = VANILLA_STACKING_RULE;
        this.stackingRule = defaultStackingRule;
    }

    public ItemStack(@NotNull Material material, byte amount, int damage) {
        this.identifier = DATA_OWNERSHIP.generateIdentifier();
        this.material = material;
        this.amount = amount;
        this.damage = damage;
        this.lore = new ArrayList<>();

        this.enchantmentMap = new Object2ShortOpenHashMap<>();
        this.attributes = new ArrayList<>();

        this.canDestroy = new HashSet<>();
        this.canPlaceOn = new HashSet<>();

        this.itemMeta = findMeta();
    }

    public ItemStack(@NotNull Material material, byte amount) {
        this(material, amount, (short) 0);
    }

    public ItemStack(@NotNull Material material) {
        this(material, (byte) 1, (short) 0);
    }

    /**
     * Gets a new {@link ItemStack} with the material sets to {@link Material#AIR}.
     * <p>
     * Used when you require a "null item".
     *
     * @return an air item
     */
    @NotNull
    public static ItemStack getAirItem() {
        return new ItemStack(Material.AIR, (byte) 0);
    }

    /**
     * Gets the default {@link StackingRule} for newly created {@link ItemStack}.
     *
     * @return the default stacking rule
     */
    @NotNull
    public static StackingRule getDefaultStackingRule() {
        return defaultStackingRule;
    }

    /**
     * Changes the default stacking rule for created item stack.
     *
     * @param defaultStackingRule the default item stack
     * @throws NullPointerException if {@code defaultStackingRule} is null
     */
    public static void setDefaultStackingRule(@NotNull StackingRule defaultStackingRule) {
        ItemStack.defaultStackingRule = defaultStackingRule;
    }

    /**
     * Loads an {@link ItemStack} from nbt.
     *
     * @param nbt the nbt compound containing the item
     * @return the parsed item stack
     */
    @NotNull
    public static ItemStack fromNBT(@NotNull NBTCompound nbt) {
        if (!nbt.containsKey("id") || !nbt.containsKey("Count"))
            throw new IllegalArgumentException("Invalid item NBT, must at least contain 'id' and 'Count' tags");
        final Material material = Registries.getMaterial(nbt.getString("id"));
        final byte count = nbt.getAsByte("Count");

        ItemStack s = new ItemStack(material, count);

        NBTCompound tag = nbt.getCompound("tag");
        if (tag != null) {
            NBTUtils.loadDataIntoItem(s, tag);
        }
        return s;
    }

    /**
     * Gets if the item material is {@link Material#AIR}.
     *
     * @return true if the material is air, false otherwise
     */
    public boolean isAir() {
        return material == Material.AIR;
    }

    /**
     * Gets if two items are similar.
     * It does not take {@link #getAmount()} and {@link #getStackingRule()} in consideration.
     *
     * @param itemStack The ItemStack to compare to
     * @return true if both items are similar
     */
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        synchronized (ItemStack.class) {
            if (itemStack.getIdentifier().equals(identifier)) {
                return true;
            }

            final boolean displayNameCheck = Objects.equals(displayName, itemStack.displayName);
            final boolean loreCheck = Objects.equals(lore, itemStack.lore);

            final Data itemData = itemStack.getData();
            final boolean dataCheck = (data == null && itemData == null) ||
                    (data != null && data.equals(itemData));

            final boolean sameMeta = (itemStack.itemMeta == null && itemMeta == null) ||
                    (itemStack.itemMeta != null && itemMeta != null && (itemStack.itemMeta.isSimilar(itemMeta)));

            return itemStack.getMaterial() == material &&
                    displayNameCheck &&
                    loreCheck &&
                    itemStack.isUnbreakable() == unbreakable &&
                    itemStack.getDamage() == damage &&
                    itemStack.enchantmentMap.equals(enchantmentMap) &&
                    itemStack.attributes.equals(attributes) &&
                    itemStack.hideFlag == hideFlag &&
                    sameMeta &&
                    dataCheck &&
                    itemStack.canPlaceOn.equals(canPlaceOn) &&
                    itemStack.canDestroy.equals(canDestroy);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemStack &&
                isSimilar((ItemStack) o) && ((ItemStack) o).getAmount() == getAmount();
    }

    /**
     * Checks if this item can be placed on the block.
     * This should be enforced only for adventure mode players.
     *
     * @param block the block's namespaceID
     * @return <code>true</code> if it can be placed, <code>false</code> otherwise
     */
    public boolean canPlaceOn(String block) {
        return canPlaceOn.contains(block);
    }

    /**
     * Gets the blocks that this item can be placed on
     *
     * @return the {@link Set} of blocks
     */
    public Set<String> getCanPlaceOn() {
        return canPlaceOn;
    }

    /**
     * Checks if this item is allowed to break the provided block.
     * This should be enforced only for adventure mode players.
     *
     * @param block the block's namespaceID
     * @return <code>true</code> if this item can destroy it, otherwise <code>false</code>
     */
    public boolean canDestroy(String block) {
        return canDestroy.contains(block);
    }

    /**
     * Gets the blocks that this item can destroy
     *
     * @return the {@link Set} of blocks
     */
    public Set<String> getCanDestroy() {
        return canDestroy;
    }

    /**
     * Gets the item damage (durability).
     *
     * @return the item damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Sets the item damage (durability).
     *
     * @param damage the item damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * Gets the item amount.
     * <p>
     * WARNING: for amount computation it would be better to use {@link StackingRule#getAmount(ItemStack)}
     * to support all stacking implementation.
     *
     * @return the item amount
     */
    public byte getAmount() {
        return amount;
    }

    /**
     * Changes the item amount.
     * <p>
     * WARNING: for amount computation it would be better to use {@link StackingRule#getAmount(ItemStack)}
     * to support all stacking implementation.
     *
     * @param amount the new item amount
     */
    public void setAmount(byte amount) {
        this.amount = amount;
    }

    /**
     * Gets the special meta object for this item.
     * <p>
     * Can be null if not any.
     *
     * @return the item meta
     */
    @Nullable
    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    /**
     * Changes the item meta linked to this item.
     * <p>
     * WARNING: be sure to have nbt data useful for this item, items should automatically get the appropriate
     * item meta.
     *
     * @param itemMeta the new item meta
     */
    public void setItemMeta(@Nullable ItemMeta itemMeta) {
        this.itemMeta = itemMeta;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name, can be null if not present
     * @deprecated Use {@link #getDisplayName()}
     */
    @Deprecated
    @Nullable
    public JsonMessage getDisplayNameJson() {
        return JsonMessage.fromComponent(displayName);
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name, can be null if not present
     */
    @Nullable
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Sets the item display name.
     *
     * @param displayName the item display name
     * @deprecated Use {@link #setDisplayName(Component)}
     */
    @Deprecated
    public void setDisplayName(@Nullable JsonMessage displayName) {
        this.setDisplayName(displayName == null ? null : displayName.asComponent());
    }

    /**
     * Sets the item display name.
     *
     * @param displayName the item display name
     */
    public void setDisplayName(@Nullable Component displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets if the item has a display name.
     *
     * @return the item display name
     */
    public boolean hasDisplayName() {
        return displayName != null;
    }

    /**
     * Gets the item lore.
     *
     * @return a modifiable list containing the item lore, can be empty if not present
     * @deprecated Use {@link #getLore()}
     */
    @Deprecated
    @NotNull
    public List<JsonMessage> getLoreJson() {
        return lore.stream().map(JsonMessage::fromComponent).collect(Collectors.toList());
    }

    /**
     * Gets the item lore.
     *
     * @return a modifiable list containing the item lore, can be empty if not present
     */
    @NotNull
    public List<Component> getLore() {
        return lore;
    }

    /**
     * Sets the item lore.
     *
     * @param lore the item lore, can be empty to remove
     * @deprecated Use {@link #setLore}
     */
    @Deprecated
    public void setLoreJson(@NotNull List<JsonMessage> lore) {
        this.lore = lore.stream().map(JsonMessage::asComponent).collect(Collectors.toList());
    }

    /**
     * Sets the item lore.
     *
     * @param lore the item lore, can be empty to remove
     */
    @NotNull
    public void setLore(List<Component> lore) {
        this.lore = lore;
    }

    /**
     * Gets if the item has a lore.
     *
     * @return true if the item has lore, false otherwise
     */
    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    /**
     * Gets the item enchantment map.
     *
     * @return an unmodifiable map containing the item enchantments
     */
    @NotNull
    public Map<Enchantment, Short> getEnchantmentMap() {
        return Collections.unmodifiableMap(enchantmentMap);
    }

    /**
     * Sets an enchantment level.
     *
     * @param enchantment the enchantment type
     * @param level       the enchantment level
     */
    public void setEnchantment(@NotNull Enchantment enchantment, short level) {
        if (level < 1) {
            removeEnchantment(enchantment);
            return;
        }

        this.enchantmentMap.put(enchantment, level);
    }

    /**
     * Removes an enchantment.
     *
     * @param enchantment the enchantment type
     */
    public void removeEnchantment(@NotNull Enchantment enchantment) {
        this.enchantmentMap.removeShort(enchantment);
    }

    /**
     * Gets an enchantment level.
     *
     * @param enchantment the enchantment type
     * @return the stored enchantment level, 0 if not present
     */
    public int getEnchantmentLevel(@NotNull Enchantment enchantment) {
        return this.enchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    /**
     * Gets the item attributes.
     *
     * @return an unmodifiable {@link List} containing the item attributes
     */
    @NotNull
    public List<ItemAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * Gets the {@link ItemAttribute} with the specified internal name.
     *
     * @param internalName the internal name of the attribute
     * @return the {@link ItemAttribute} with the internal name, null if not found
     */
    public ItemAttribute getAttribute(@NotNull String internalName) {
        for (ItemAttribute itemAttribute : attributes) {
            if (itemAttribute.getInternalName().equals(internalName))
                return itemAttribute;
        }
        return null;
    }

    /**
     * Adds an attribute to the item.
     *
     * @param itemAttribute the attribute to add
     */
    public void addAttribute(@NotNull ItemAttribute itemAttribute) {
        this.attributes.add(itemAttribute);
    }

    /**
     * Removes an attribute to the item.
     *
     * @param itemAttribute the attribute to remove
     */
    public void removeAttribute(@NotNull ItemAttribute itemAttribute) {
        this.attributes.remove(itemAttribute);
    }

    /**
     * Gets the item hide flag.
     *
     * @return the item hide flag
     */
    public int getHideFlag() {
        return hideFlag;
    }

    /**
     * Changes the item hide flag. This is the integer sent when updating the item hide flag.
     *
     * @param hideFlag the new item hide flag
     */
    public void setHideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
    }

    /**
     * Gets the item custom model data.
     *
     * @return the item custom model data
     */
    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * Changes the item custom model data.
     *
     * @param customModelData the new item custom data model
     */
    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    /**
     * Adds flags to the item.
     *
     * @param flags the flags to add
     */
    public void addItemFlags(@NotNull ItemFlag... flags) {
        for (ItemFlag f : flags) {
            this.hideFlag |= getBitModifier(f);
        }
    }

    /**
     * Removes flags from the item.
     *
     * @param flags the flags to remove
     */
    public void removeItemFlags(@NotNull ItemFlag... flags) {
        for (ItemFlag f : flags) {
            this.hideFlag &= ~getBitModifier(f);
        }
    }

    /**
     * Gets the item flags.
     *
     * @return an unmodifiable {@link Set} containing the item flags
     */
    @NotNull
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
     * Gets if the item has an item flag.
     *
     * @param flag the item flag
     * @return true if the item has the flag {@code flag}, false otherwise
     */
    public boolean hasItemFlag(@NotNull ItemFlag flag) {
        final int bitModifier = getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    /**
     * Gets if the item is unbreakable.
     *
     * @return true if the item is unbreakable, false otherwise
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * Makes the item unbreakable.
     *
     * @param unbreakable true to make the item unbreakable, false otherwise
     */
    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    /**
     * Gets the unique identifier of this object.
     * <p>
     * This value is non persistent and will be randomized once this item is separated with a right-click,
     * when copied and when the server restart. It is used internally by the data ownership system.
     *
     * @return this item unique identifier
     */
    @NotNull
    public UUID getIdentifier() {
        return identifier;
    }

    /**
     * Gets the item {@link Material}.
     *
     * @return the item material
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * Changes the item {@link Material}.
     *
     * @param material the new material
     */
    public void setMaterial(@NotNull Material material) {
        this.material = material;
    }

    /**
     * Gets if the item has any nbt tag.
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
                (itemMeta != null && itemMeta.hasNbt()) ||
                (data != null && !data.isEmpty()) ||
                !canDestroy.isEmpty() ||
                !canPlaceOn.isEmpty();
    }

    /**
     * @deprecated use {@link #clone()}
     */
    @Deprecated
    @NotNull
    public synchronized ItemStack copy() {
        return clone();
    }

    /**
     * Clones this item stack.
     * <p>
     * Be aware that the identifier ({@link #getIdentifier()}) will change.
     *
     * @return a cloned item stack with a different identifier
     */
    @NotNull
    @Override
    public ItemStack clone() {
        try {
            ItemStack itemStack = (ItemStack) super.clone();
            itemStack.setDisplayName(displayName);
            itemStack.setUnbreakable(unbreakable);
            if (lore != null) {
                itemStack.setLore(new ArrayList<>(lore));
            }
            if (stackingRule != null) {
                itemStack.setStackingRule(stackingRule);
            }

            itemStack.enchantmentMap = new Object2ShortOpenHashMap<>(enchantmentMap);
            itemStack.attributes = new ArrayList<>(attributes);

            itemStack.hideFlag = hideFlag;
            itemStack.customModelData = customModelData;

            itemStack.canPlaceOn = new HashSet<>(canPlaceOn);
            itemStack.canDestroy = new HashSet<>(canDestroy);

            if (itemMeta != null)
                itemStack.itemMeta = itemMeta.clone();

            final Data data = getData();
            if (data != null)
                itemStack.setData(data.clone());

            return itemStack;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    @Nullable
    @Override
    public Data getData() {
        return data;
    }

    /**
     * Sets the data of this item.
     *
     * @param data the new {@link Data} of this container, null to remove it
     */
    @Override
    public void setData(@Nullable Data data) {
        DATA_OWNERSHIP.saveOwnObject(getIdentifier(), data);
        this.data = data;
    }

    /**
     * Gets the item {@link StackingRule}.
     *
     * @return the item stacking rule
     */
    @NotNull
    public StackingRule getStackingRule() {
        return stackingRule;
    }

    /**
     * Changes the {@link StackingRule} of the item.
     *
     * @param stackingRule the new item stacking rule
     * @throws NullPointerException if {@code stackingRule} is null
     */
    public void setStackingRule(@NotNull StackingRule stackingRule) {
        this.stackingRule = stackingRule;
    }

    /**
     * Consumes this item by a specific amount.
     * <p>
     * Will return null if the amount's amount isn't enough.
     *
     * @param amount the quantity to consume
     * @return the new item with the updated amount, null if the item cannot be consumed by this much
     */
    @Nullable
    public ItemStack consume(int amount) {
        final int currentAmount = stackingRule.getAmount(this);
        if (currentAmount < amount)
            return null;
        return stackingRule.apply(this, currentAmount - amount);
    }

    private byte getBitModifier(@NotNull ItemFlag hideFlag) {
        return (byte) (1 << hideFlag.ordinal());
    }

    /**
     * Finds the {@link ItemMeta} based on the material type.
     *
     * @return the item meta, null if none found
     */
    @Nullable
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

        if (material == Material.FIREWORK_STAR)
            return new FireworkEffectMeta();

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

    /**
     * Creates a {@link NBTCompound} containing the data of this item.
     * <p>
     * WARNING: modifying the returned nbt will not affect the item.
     *
     * @return this item nbt
     */
    @NotNull
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
     * WARNING: not implemented yet.
     * <p>
     * This is be called each time an item is serialized to be send to a player,
     * can be used to customize the display of the item based on player data.
     *
     * @param player the player
     * @return the custom {@link ItemDisplay} for {@code player},
     * null to use the normal item display name &amp; lore
     */
    public ItemDisplay getCustomDisplay(Player player) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public @NotNull HoverEvent<ShowItem> asHoverEvent(@NotNull UnaryOperator<ShowItem> op) {
        return HoverEvent.showItem(op.apply(ShowItem.of(this.material, this.amount, NBTUtils.asBinaryTagHolder(this.toNBT().getCompound("tag")))));
    }

    // Callback events

    /**
     * Called when the player right clicks with this item.
     *
     * @param player the player who used the item
     * @param hand   the hand used
     */
    public void onRightClick(@NotNull Player player, @NotNull Player.Hand hand) {
    }

    /**
     * Called when the player left clicks with this item.
     *
     * @param player the player who used the item
     * @param hand   the hand used
     */
    public void onLeftClick(@NotNull Player player, @NotNull Player.Hand hand) {
    }

    /**
     * Called when the player right clicks with this item on a block.
     *
     * @param player    the player who used the item
     * @param hand      the hand used
     * @param position  the position of the interacted block
     * @param blockFace the block face
     * @return true if it prevents normal item use (placing blocks for instance)
     */
    public boolean onUseOnBlock(@NotNull Player player, @NotNull Player.Hand hand, @NotNull BlockPosition position, @NotNull Direction blockFace) {
        return false;
    }

    /**
     * Called when the player click on this item on an inventory.
     * <p>
     * Executed before any events.
     *
     * @param player          the player who clicked on the item
     * @param clickType       the click type
     * @param slot            the slot clicked
     * @param playerInventory true if the click is in the player inventory
     */
    public void onInventoryClick(@NotNull Player player, @NotNull ClickType clickType, int slot, boolean playerInventory) {

    }
}
