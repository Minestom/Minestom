package fr.themode.minestom.inventory;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.click.InventoryClickProcessor;
import fr.themode.minestom.inventory.click.InventoryClickResult;
import fr.themode.minestom.inventory.condition.InventoryCondition;
import fr.themode.minestom.inventory.condition.InventoryConditionResult;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;
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

    private InventoryCondition inventoryCondition;
    private InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

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

    @Override
    public InventoryCondition getInventoryCondition() {
        return inventoryCondition;
    }

    @Override
    public void setInventoryCondition(InventoryCondition inventoryCondition) {
        this.inventoryCondition = inventoryCondition;
    }

    public void update() {
        WindowItemsPacket windowItemsPacket = getWindowItemsPacket();
        sendPacketToViewers(windowItemsPacket);
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
            sendPacketToViewers(setSlotPacket);
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
        ItemStack cursor = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);


        InventoryClickResult clickResult = clickProcessor.leftClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            player.getPlayerConnection().sendPacket(getWindowItemsPacket());

        System.out.println("cancel:" + clickResult.isCancel());

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        }
    }

    @Override
    public void rightClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack cursor = getCursorItem(player);
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        InventoryClickResult clickResult = clickProcessor.rightClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            player.getPlayerConnection().sendPacket(getWindowItemsPacket());

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
            setCursorPlayerItem(player, clickResult.getCursor());
        }
    }

    @Override
    public void shiftClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        ItemStack cursorItem = getCursorItem(player); // Isn't used in the algorithm

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                //System.out.println(clicked.getMaterial() + " + " + cursorItem.getMaterial());
                if (isInWindow) {
                    setItemStack(slot, clicked);
                    setCursorPlayerItem(player, cursorItem);
                } else {
                    playerInventory.setItemStack(slot, offset, clicked);
                    setCursorPlayerItem(player, cursorItem);
                }
                // Refresh client window
                player.getPlayerConnection().sendPacket(getWindowItemsPacket());
                return;
            }
        }
        // End condition

        if (clicked.isAir())
            return;

        ItemStack resultClicked = clicked.clone();
        boolean filled = false;

        StackingRule clickedRule = clicked.getStackingRule();
        int maxSize = clickedRule.getMaxSize();

        if (!isInWindow) {
            for (int i = 0; i < itemStacks.length; i++) {
                ItemStack item = itemStacks[i];
                if (clickedRule.canBeStacked(clicked, item)) {
                    int amount = item.getAmount();
                    if (amount == maxSize)
                        continue;
                    int totalAmount = resultClicked.getAmount() + amount;
                    if (!clickedRule.canApply(clicked, totalAmount)) {
                        item = clickedRule.apply(item, maxSize);
                        setItemStack(i, item);
                        resultClicked = clickedRule.apply(resultClicked, totalAmount - maxSize);
                        filled = false;
                        continue;
                    } else {
                        resultClicked = clickedRule.apply(resultClicked, totalAmount);
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
                if (clickedRule.canBeStacked(clicked, item)) {
                    int amount = item.getAmount();
                    if (amount == maxSize)
                        continue;
                    int totalAmount = resultClicked.getAmount() + amount;
                    if (!clickedRule.canApply(clicked, totalAmount)) {
                        item = clickedRule.apply(item, maxSize);
                        playerInventory.setItemStack(i, offset, item);
                        resultClicked = clickedRule.apply(resultClicked, totalAmount - maxSize);
                        filled = false;
                        continue;
                    } else {
                        resultClicked = clickedRule.apply(resultClicked, totalAmount);
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
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);
        ItemStack heldItem = playerInventory.getItemStack(key);

        InventoryClickResult clickResult = clickProcessor.changeHeld(getInventoryCondition(), player, slot, clicked, heldItem);

        if (clickResult.doRefresh())
            player.getPlayerConnection().sendPacket(getWindowItemsPacket());

        if (isInWindow) {
            setItemStack(slot, clickResult.getClicked());
        } else {
            playerInventory.setItemStack(slot, offset, clickResult.getClicked());
        }
        playerInventory.setItemStack(key, clickResult.getCursor());
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
    public void dragging(Player player, int slot, int button) {
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = null;
        ItemStack cursor = getCursorItem(player);
        if (slot != -999)
            clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset);

        InventoryClickResult clickResult = clickProcessor.dragging(getInventoryCondition(), player,
                slot, button,
                clicked, cursor,

                s -> isClickInWindow(s) ? getItemStack(s) : playerInventory.getItemStack(s, offset),

                (s, item) -> {
                    System.out.println("click window:" + isClickInWindow(s));
                    if (isClickInWindow(s)) {
                        setItemStack(s, item);
                    } else {
                        playerInventory.setItemStack(s, offset, item);
                    }
                });

        if (clickResult == null)
            return;

        if (isInWindow) {
            setCursorPlayerItem(player, clickResult.getCursor());
        } else {
            setCursorPlayerItem(player, clickResult.getCursor());
        }
    }

    @Override
    public void doubleClick(Player player, int slot) {
        PlayerInventory playerInventory = player.getInventory();
        boolean isInWindow = isClickInWindow(slot);
        ItemStack clicked = isInWindow ? getItemStack(slot) : playerInventory.getItemStack(slot, offset); // Isn't used in the algorithm
        ItemStack cursorItem = getCursorItem(player);

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                //System.out.println(clicked.getMaterial() + " + " + cursorItem.getMaterial());
                if (isInWindow) {
                    setItemStack(slot, clicked);
                    setCursorPlayerItem(player, cursorItem);
                } else {
                    playerInventory.setItemStack(slot, offset, clicked);
                    setCursorPlayerItem(player, cursorItem);
                }
                // Refresh client window
                player.getPlayerConnection().sendPacket(getWindowItemsPacket());
                return;
            }
        }
        // End condition

        if (cursorItem.isAir())
            return;

        int amount = cursorItem.getAmount();

        StackingRule cursorRule = cursorItem.getStackingRule();
        int maxSize = cursorRule.getMaxSize();

        if (amount == maxSize)
            return;

        // Start by looping through the opened inventory
        for (int i = 0; i < itemStacks.length; i++) {
            if (i == slot)
                continue;
            if (amount == maxSize)
                break;
            ItemStack item = itemStacks[i];
            if (cursorRule.canBeStacked(cursorItem, item)) {
                int totalAmount = amount + item.getAmount();
                if (!cursorRule.canApply(cursorItem, totalAmount)) {
                    cursorItem = cursorRule.apply(cursorItem, maxSize);
                    item = cursorRule.apply(item, totalAmount - maxSize);
                    setItemStack(i, item);
                } else {
                    cursorItem = cursorRule.apply(cursorItem, totalAmount);
                    setItemStack(i, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        // Looping through player inventory
        for (int i = 9; i < PlayerInventory.INVENTORY_SIZE - 9; i++) { // Inventory
            if (playerInventory.convertToPacketSlot(i) == slot)
                continue;
            if (amount == maxSize)
                break;
            ItemStack item = playerInventory.getItemStack(i);
            if (cursorRule.canBeStacked(cursorItem, item)) {
                int totalAmount = amount + item.getAmount();
                if (!cursorRule.canApply(cursorItem, totalAmount)) {
                    cursorItem = cursorRule.apply(cursorItem, maxSize);
                    item = cursorRule.apply(item, totalAmount - maxSize);
                    playerInventory.setItemStack(i, offset, item);
                } else {
                    cursorItem = cursorRule.apply(cursorItem, totalAmount);
                    playerInventory.setItemStack(i, offset, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        for (int i = 0; i < 9; i++) { // Hotbar
            if (playerInventory.convertToPacketSlot(i) == slot)
                continue;
            if (amount == maxSize)
                break;
            ItemStack item = playerInventory.getItemStack(i);
            if (cursorRule.canBeStacked(cursorItem, item)) {
                int totalAmount = amount + item.getAmount();
                if (!cursorRule.canApply(cursorItem, totalAmount)) {
                    cursorItem = cursorRule.apply(cursorItem, maxSize);
                    item = cursorRule.apply(item, totalAmount - maxSize);
                    playerInventory.setItemStack(i, offset, item);
                } else {
                    cursorItem = cursorRule.apply(cursorItem, totalAmount);
                    playerInventory.setItemStack(i, offset, ItemStack.AIR_ITEM);
                }
                amount = cursorItem.getAmount();
            }
        }

        setCursorPlayerItem(player, cursorItem);
        playerInventory.update();
    }
}
