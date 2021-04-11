package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataContainer;
import net.minestom.server.inventory.click.InventoryClickProcessor;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * Represents an inventory where items can be modified/retrieved.
 */
public abstract class AbstractInventory implements InventoryClickHandler, DataContainer {

    private final int size;
    protected final ItemStack[] itemStacks;

    // list of conditions/callbacks assigned to this inventory
    protected final List<InventoryCondition> inventoryConditions = new CopyOnWriteArrayList<>();
    // the click processor which process all the clicks in the inventory
    protected final InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    private Data data;

    protected AbstractInventory(int size) {
        this.size = size;
        this.itemStacks = new ItemStack[getSize()];

        Arrays.fill(itemStacks, ItemStack.AIR);
    }

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    public abstract void setItemStack(int slot, @NotNull ItemStack itemStack);

    protected abstract void safeItemInsert(int slot, @NotNull ItemStack itemStack);

    /**
     * Adds an {@link ItemStack} to the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to add
     * @return true if the item has been successfully added, false otherwise
     */
    public synchronized <T> T addItemStack(@NotNull ItemStack itemStack, FillOption<T> fillOption) {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();

        final StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = 0; i < getInnerSize(); i++) {
            ItemStack inventoryItem = getItemStack(i);
            if (inventoryItem.isAir()) {
                continue;
            }
            if (stackingRule.canBeStacked(itemStack, inventoryItem)) {
                final int itemAmount = stackingRule.getAmount(inventoryItem);
                if (itemAmount == stackingRule.getMaxSize())
                    continue;
                final int itemStackAmount = stackingRule.getAmount(itemStack);
                final int totalAmount = itemStackAmount + itemAmount;
                if (!stackingRule.canApply(itemStack, totalAmount)) {
                    // Slot cannot accept the whole item, reduce amount to 'itemStack'
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, stackingRule.getMaxSize()));
                    itemStack = stackingRule.apply(itemStack, totalAmount - stackingRule.getMaxSize());
                } else {
                    // Slot can accept the whole item
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, totalAmount));
                    itemStack = ItemStack.AIR;
                    break;
                }
            }
        }
        for (int i = 0; i < getInnerSize(); i++) {
            ItemStack inventoryItem = getItemStack(i);
            if (!inventoryItem.isAir()) {
                continue;
            }
            // Fill the slot
            itemChangesMap.put(i, itemStack);
            itemStack = ItemStack.AIR;
            break;
        }

        return fillOption.fill(this, itemStack, itemChangesMap);
    }

    public synchronized boolean addItemStack(@NotNull ItemStack itemStack) {
        return addItemStack(itemStack, FillOption.ALL_OR_NOTHING);
    }

    /**
     * Adds {@link ItemStack}s to the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to add
     * @return the operation results
     */
    public <T> @NotNull List<T> addItemStacks(@NotNull List<ItemStack> itemStacks, @NotNull FillOption<T> fillOption) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(itemStack -> {
            T fillResult = addItemStack(itemStack, fillOption);
            result.add(fillResult);
        });
        return result;
    }

    /**
     * Takes an {@link ItemStack} from the inventory and sends relevant update to the viewer(s).
     *
     * @param itemStack the item to take
     * @return true if the item has been successfully fully taken, false otherwise
     */
    public <T> T takeItemStack(@NotNull ItemStack itemStack, @NotNull FillOption<T> fillOption) {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();
        final StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = 0; i < getInnerSize(); i++) {
            ItemStack inventoryItem = getItemStack(i);
            if (inventoryItem.isAir()) {
                continue;
            }
            if (stackingRule.canBeStacked(itemStack, inventoryItem)) {
                final int itemAmount = stackingRule.getAmount(inventoryItem);
                final int itemStackAmount = stackingRule.getAmount(itemStack);
                if (itemStackAmount < itemAmount) {
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, itemAmount - itemStackAmount));
                    itemStack = ItemStack.AIR;
                    break;
                }
                itemChangesMap.put(i, ItemStack.AIR);
                itemStack = stackingRule.apply(itemStack, itemStackAmount - itemAmount);
                if (stackingRule.getAmount(itemStack) == 0) {
                    itemStack = ItemStack.AIR;
                    break;
                }
            }
        }

        return fillOption.fill(this, itemStack, itemChangesMap);
    }

    /**
     * Takes {@link ItemStack}s from the inventory and sends relevant updates to the viewer(s).
     *
     * @param itemStacks items to take
     * @return the operation results
     */
    public <T> @NotNull List<T> takeItemStacks(@NotNull List<ItemStack> itemStacks, @NotNull FillOption<T> fillOption) {
        List<T> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(itemStack -> {
            T fillResult = takeItemStack(itemStack, fillOption);
            result.add(fillResult);
        });
        return result;
    }

    public synchronized void replaceItemStack(int slot, @NotNull UnaryOperator<@NotNull ItemStack> operator) {
        var currentItem = getItemStack(slot);
        setItemStack(slot, operator.apply(currentItem));
    }

    /**
     * Clears the inventory and send relevant update to the viewer(s).
     */
    public synchronized void clear() {
        // Clear the item array
        Arrays.fill(itemStacks, ItemStack.AIR);
        // Send the cleared inventory to viewers
        update();
    }

    public abstract void update();

    /**
     * Gets the {@link ItemStack} at the specified slot.
     *
     * @param slot the slot to check
     * @return the item in the slot {@code slot}
     */
    public @NotNull ItemStack getItemStack(int slot) {
        return itemStacks[slot];
    }

    /**
     * Gets all the {@link ItemStack} in the inventory.
     * <p>
     * Be aware that the returned array does not need to be the original one,
     * meaning that modifying it directly may not work.
     *
     * @return an array containing all the inventory's items
     */
    public @NotNull ItemStack[] getItemStacks() {
        return itemStacks.clone();
    }

    /**
     * Gets the size of the inventory.
     *
     * @return the inventory's size
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the size of the "inner inventory" (which includes only "usable" slots).
     *
     * @return inner inventory's size
     */
    public int getInnerSize() {
        return getSize();
    }

    /**
     * Gets all the {@link InventoryCondition} of this inventory.
     *
     * @return a modifiable {@link List} containing all the inventory conditions
     */
    public @NotNull List<InventoryCondition> getInventoryConditions() {
        return inventoryConditions;
    }

    /**
     * Adds a new {@link InventoryCondition} to this inventory.
     *
     * @param inventoryCondition the inventory condition to add
     */
    public void addInventoryCondition(@NotNull InventoryCondition inventoryCondition) {
        this.inventoryConditions.add(inventoryCondition);
    }

    /**
     * Places all the items of {@code itemStacks} into the internal array.
     *
     * @param itemStacks the array to copy the content from
     * @throws IllegalArgumentException if the size of the array is not equal to {@link #getSize()}
     * @throws NullPointerException     if {@code itemStacks} contains one null element or more
     */
    public void copyContents(@NotNull ItemStack[] itemStacks) {
        Check.argCondition(itemStacks.length != getSize(),
                "The size of the array has to be of the same size as the inventory: " + getSize());

        for (int i = 0; i < itemStacks.length; i++) {
            final ItemStack itemStack = itemStacks[i];
            Check.notNull(itemStack, "The item array cannot contain any null element!");
            setItemStack(i, itemStack);
        }
    }

    @Override
    public @Nullable Data getData() {
        return data;
    }

    @Override
    public void setData(@Nullable Data data) {
        this.data = data;
    }
}
