package fr.themode.minestom.inventory;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class Inventory implements InventoryModifier, InventoryClickHandler, Viewable {

    private static AtomicInteger lastInventoryId = new AtomicInteger();

    private int id;
    private InventoryType inventoryType;
    private String title;

    private int offset;

    private ItemStack[] itemStacks;
    private Set<Player> viewers = new CopyOnWriteArraySet<>();
    private ConcurrentHashMap<Player, ItemStack> cursorPlayersItem = new ConcurrentHashMap<>();

    public Inventory(InventoryType inventoryType, String title) {
        this.id = generateId();
        this.inventoryType = inventoryType;
        this.title = title;

        this.offset = inventoryType.getAdditionalSlot();

        this.itemStacks = new ItemStack[inventoryType.getAdditionalSlot()];
        Arrays.fill(itemStacks, ItemStack.AIR_ITEM);
    }

    private static int generateId() {
        return lastInventoryId.incrementAndGet();
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public String getTitle() {
        return title;
    }

    public int getWindowId() {
        return id;
    }

    @Override
    public void setItemStack(int slot, ItemStack itemStack) {
        if (slot < 0 || slot > inventoryType.getAdditionalSlot())
            throw new IllegalArgumentException(inventoryType.toString() + " does not have slot " + slot);

        safeItemInsert(slot, itemStack);
    }

    @Override
    public boolean addItemStack(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return itemStacks[slot];
    }

    @Override
    public ItemStack[] getItemStacks() {
        return Arrays.copyOf(itemStacks, itemStacks.length);
    }

    public void update() {
        WindowItemsPacket windowItemsPacket = getWindowItemsPacket();
        getViewers().forEach(p -> p.getPlayerConnection().sendPacket(windowItemsPacket));
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public void addViewer(Player player) {
        this.viewers.add(player);
        WindowItemsPacket windowItemsPacket = getWindowItemsPacket();
        player.getPlayerConnection().sendPacket(windowItemsPacket);
    }

    @Override
    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    public ItemStack getCursorItem(Player player) {
        return cursorPlayersItem.getOrDefault(player, ItemStack.AIR_ITEM);
    }

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.AIR_ITEM : itemStack;
            this.itemStacks[slot] = itemStack;
            SetSlotPacket setSlotPacket = new SetSlotPacket();
            setSlotPacket.windowId = 1;
            setSlotPacket.slot = (short) slot;
            setSlotPacket.itemStack = itemStack;
            getViewers().forEach(player -> player.getPlayerConnection().sendPacket(setSlotPacket));
        }
    }

    private WindowItemsPacket getWindowItemsPacket() {
        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = getWindowId();
        windowItemsPacket.count = (short) itemStacks.length;
        windowItemsPacket.items = itemStacks;
        return windowItemsPacket;
    }

    private void setCursorPlayerItem(Player player, ItemStack itemStack) {
        this.cursorPlayersItem.put(player, itemStack);
    }

    private boolean isClickInWindow(int slot) {
        return slot < inventoryType.getAdditionalSlot();
    }

    @Override
    public void leftClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursorItem = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        if (cursorItem.isAir() && clicked.isAir())
            return;

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorItem.isSimilar(clicked)) {
            resultCursor = cursorItem.clone();
            resultClicked = clicked.clone();
            int totalAmount = cursorItem.getAmount() + clicked.getAmount();
            if (totalAmount > 64) {
                resultCursor.setAmount((byte) (totalAmount - 64));
                resultClicked.setAmount((byte) 64);
            } else {
                resultCursor = ItemStack.AIR_ITEM;
                resultClicked.setAmount((byte) totalAmount);
            }
        } else {
            resultCursor = clicked.clone();
            resultClicked = cursorItem.clone();
        }

        if (isInWindow) {
            setItemStack(slot, resultClicked);
            setCursorPlayerItem(player, resultCursor);
        } else {
            playerInventory.setItemStack(slot, offset, resultClicked);
            setCursorPlayerItem(player, resultCursor);
        }
    }

    @Override
    public void rightClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursorItem = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        if (cursorItem.isAir() && clicked.isAir())
            return;

        ItemStack resultCursor;
        ItemStack resultClicked;

        if (cursorItem.isSimilar(clicked)) {
            resultClicked = clicked.clone();
            int amount = clicked.getAmount() + 1;
            if (amount > 64) {
                return;
            } else {
                resultCursor = cursorItem.clone();
                resultCursor.setAmount((byte) (resultCursor.getAmount() - 1));
                if (resultCursor.getAmount() < 1)
                    resultCursor = ItemStack.AIR_ITEM;
                resultClicked.setAmount((byte) amount);
            }
        } else {
            if (cursorItem.isAir()) {
                int amount = (int) Math.ceil((double) clicked.getAmount() / 2d);
                resultCursor = clicked.clone();
                resultCursor.setAmount((byte) amount);
                resultClicked = clicked.clone();
                resultClicked.setAmount((byte) (clicked.getAmount() / 2));
            } else {
                if (clicked.isAir()) {
                    int amount = cursorItem.getAmount();
                    resultCursor = cursorItem.clone();
                    resultCursor.setAmount((byte) (amount - 1));
                    if (resultCursor.getAmount() < 1)
                        resultCursor = ItemStack.AIR_ITEM;
                    resultClicked = cursorItem.clone();
                    resultClicked.setAmount((byte) 1);
                } else {
                    resultCursor = clicked.clone();
                    resultClicked = cursorItem.clone();
                }
            }
        }

        if (isInWindow) {
            setItemStack(slot, resultClicked);
            setCursorPlayerItem(player, resultCursor);
        } else {
            playerInventory.setItemStack(slot, offset, resultClicked);
            setCursorPlayerItem(player, resultCursor);
        }
    }

    @Override
    public void shiftClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        if (clicked.isAir())
            return;

        ItemStack resultClicked = clicked.clone();
        boolean filled = false;

        if (!isInWindow) {
            for (int i = 0; i < itemStacks.length; i++) {
                ItemStack item = itemStacks[i];
                if (item.isSimilar(clicked)) {
                    int amount = item.getAmount();
                    if (amount == 64)
                        continue;
                    int totalAmount = resultClicked.getAmount() + amount;
                    if (totalAmount > 64) {
                        item.setAmount((byte) 64);
                        setItemStack(i, item);
                        resultClicked.setAmount((byte) (totalAmount - 64));
                        filled = false;
                        continue;
                    } else {
                        resultClicked.setAmount((byte) totalAmount);
                        setItemStack(i, resultClicked);
                        playerInventory.setItemStack(slot, offset, ItemStack.AIR_ITEM);
                        filled = true;
                        break;
                    }
                } else if (item.isAir()) {
                    // Switch
                    setItemStack(i, resultClicked);
                    playerInventory.setItemStack(slot, offset, ItemStack.AIR_ITEM);
                    filled = true;
                    break;
                }
            }
            if (!filled) {
                playerInventory.setItemStack(slot, offset, resultClicked);
            }
        } else {
            for (int i = 44; i >= 0; i--) { // Hotbar
                ItemStack item = playerInventory.getItemStack(i, offset);
                if (item.isSimilar(clicked)) {
                    int amount = item.getAmount();
                    if (amount == 64)
                        continue;
                    int totalAmount = resultClicked.getAmount() + amount;
                    if (totalAmount > 64) {
                        item.setAmount((byte) 64);
                        playerInventory.setItemStack(i, offset, item);
                        resultClicked.setAmount((byte) (totalAmount - 64));
                        filled = false;
                        continue;
                    } else {
                        resultClicked.setAmount((byte) totalAmount);
                        playerInventory.setItemStack(i, offset, resultClicked);
                        setItemStack(slot, ItemStack.AIR_ITEM);
                        filled = true;
                        break;
                    }
                } else if (item.isAir()) {
                    // Switch
                    playerInventory.setItemStack(i, offset, resultClicked);
                    setItemStack(slot, ItemStack.AIR_ITEM);
                    filled = true;
                    break;
                }
            }
            if (!filled) { // Still not filled, inventory is full
                setItemStack(slot, resultClicked);
            }
        }
    }

    @Override
    public void changeHeld(Player player, int slot, int key) {
        PlayerInventory playerInventory = player.getInventory();

        if (!getCursorItem(player).isAir())
            return;

        boolean isInWindow = isClickInWindow(slot);
        ItemStack heldItem = playerInventory.getItemStack(key);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        ItemStack resultClicked;
        ItemStack resultHeld;

        if (clicked.isAir()) {
            // Set held item [key] to slot
            resultClicked = ItemStack.AIR_ITEM;
            resultHeld = clicked.clone();
        } else {
            if (heldItem.isAir()) {
                // if held item [key] is air then set clicked to held
                resultClicked = ItemStack.AIR_ITEM;
                resultHeld = clicked.clone();
            } else {
                // Otherwise replace held item and held
                resultClicked = heldItem.clone();
                resultHeld = clicked.clone();
            }
        }

        if (isInWindow) {
            setItemStack(slot, resultClicked);
        } else {
            playerInventory.setItemStack(slot, offset, resultClicked);
        }
        playerInventory.setItemStack(key, resultHeld);
    }

    @Override
    public void middleClick(Player player, int slot) {

    }

    @Override
    public void dropOne(Player player, int slot) {

    }

    @Override
    public void dropItemStack(Player player, int slot) {

    }

    @Override
    public void doubleClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursorItem = getCursorItem(player).clone();
        if (cursorItem.isAir())
            return;

        int amount = cursorItem.getAmount();

        if (amount == 64)
            return;

        // Start by looping through the opened inventory
        for (int i = 0; i < itemStacks.length; i++) {
            if (i == slot)
                continue;
            if (amount == 64)
                break;
            ItemStack item = itemStacks[i];
            if (cursorItem.isSimilar(item)) {
                int totalAmount = amount + item.getAmount();
                if (totalAmount > 64) {
                    cursorItem.setAmount((byte) 64);
                    item.setAmount((byte) (totalAmount - 64));
                    setItemStack(i, item);
                } else {
                    cursorItem.setAmount((byte) totalAmount);
                    setItemStack(i, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        // Looping through player inventory
        for (int i = 9; i < PlayerInventory.INVENTORY_SIZE - 9; i++) { // Inventory
            if (playerInventory.convertToPacketSlot(i) == slot)
                continue;
            if (amount == 64)
                break;
            ItemStack item = playerInventory.getItemStack(i);
            if (cursorItem.isSimilar(item)) {
                int totalAmount = amount + item.getAmount();
                if (totalAmount > 64) {
                    cursorItem.setAmount((byte) 64);
                    item.setAmount((byte) (totalAmount - 64));
                    playerInventory.setItemStack(i, offset, item);
                } else {
                    cursorItem.setAmount((byte) totalAmount);
                    playerInventory.setItemStack(i, offset, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        for (int i = 0; i < 9; i++) { // Hotbar
            if (playerInventory.convertToPacketSlot(i) == slot)
                continue;
            if (amount == 64)
                break;
            ItemStack item = playerInventory.getItemStack(i);
            if (cursorItem.isSimilar(item)) {
                int totalAmount = amount + item.getAmount();
                if (totalAmount > 64) {
                    cursorItem.setAmount((byte) 64);
                    item.setAmount((byte) (totalAmount - 64));
                    playerInventory.setItemStack(i, offset, item);
                } else {
                    cursorItem.setAmount((byte) totalAmount);
                    playerInventory.setItemStack(i, offset, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        setCursorPlayerItem(player, cursorItem);
        playerInventory.update();
    }
}
