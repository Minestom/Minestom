package fr.themode.minestom.inventory;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.click.InventoryClickLoopHandler;
import fr.themode.minestom.inventory.click.InventoryClickProcessor;
import fr.themode.minestom.inventory.click.InventoryClickResult;
import fr.themode.minestom.inventory.condition.InventoryCondition;
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
        ItemStack cursor = getCursorItem(player); // Isn't used in the algorithm


        InventoryClickResult clickResult;

        if (isInWindow) {
            clickResult = clickProcessor.shiftClick(getInventoryCondition(), player, slot, clicked, cursor,
                    // Player inventory loop
                    new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE, 1,
                            i -> playerInventory.convertToPacketSlot(i),
                            index -> isClickInWindow(index) ? getItemStack(index) : playerInventory.getItemStack(index, offset),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(index, offset, itemStack);
                                }
                            }));
        } else {
            clickResult = clickProcessor.shiftClick(getInventoryCondition(), player, slot, clicked, cursor,
                    // Window loop
                    new InventoryClickLoopHandler(0, itemStacks.length, 1,
                            i -> i,
                            index -> isClickInWindow(index) ? getItemStack(index) : playerInventory.getItemStack(index, offset),
                            (index, itemStack) -> {
                                if (isClickInWindow(index)) {
                                    setItemStack(index, itemStack);
                                } else {
                                    playerInventory.setItemStack(index, offset, itemStack);
                                }
                            }));
        }

        if (clickResult == null)
            return;

        if (clickResult.doRefresh())
            update();

        setCursorPlayerItem(player, clickResult.getCursor());
        playerInventory.update();
        update();
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
        ItemStack cursor = getCursorItem(player);


        InventoryClickResult clickResult = clickProcessor.doubleClick(getInventoryCondition(), player, slot, cursor,
                // Start by looping through the opened inventory
                new InventoryClickLoopHandler(0, itemStacks.length, 1,
                        i -> i,
                        index -> itemStacks[index],
                        (index, itemStack) -> setItemStack(index, itemStack)),
                // Looping through player inventory
                new InventoryClickLoopHandler(0, PlayerInventory.INVENTORY_SIZE - 9, 1,
                        i -> playerInventory.convertToPacketSlot(i),
                        index -> playerInventory.getItemStack(index, offset),
                        (index, itemStack) -> playerInventory.setItemStack(index, offset, itemStack)),
                // Player hotbar
                new InventoryClickLoopHandler(0, 9, 1,
                        i -> playerInventory.convertToPacketSlot(i),
                        index -> playerInventory.getItemStack(index, offset),
                        (index, itemStack) -> {
                            playerInventory.setItemStack(index, offset, itemStack);
                        }));

        if (clickResult == null)
            return;

        if (clickResult.doRefresh())
            update();

        setCursorPlayerItem(player, clickResult.getCursor());
        playerInventory.update();
    }
}
