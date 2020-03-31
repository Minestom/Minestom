package fr.themode.minestom.inventory;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.click.InventoryClickProcessor;
import fr.themode.minestom.inventory.click.InventoryClickResult;
import fr.themode.minestom.inventory.rule.InventoryCondition;
import fr.themode.minestom.inventory.rule.InventoryConditionResult;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;
import fr.themode.minestom.net.packet.server.play.EntityEquipmentPacket;
import fr.themode.minestom.net.packet.server.play.SetSlotPacket;
import fr.themode.minestom.net.packet.server.play.WindowItemsPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.*;

import static fr.themode.minestom.utils.inventory.PlayerInventoryUtils.*;

public class PlayerInventory implements InventoryModifier, InventoryClickHandler {

    public static final int INVENTORY_SIZE = 46;

    private Player player;
    private ItemStack[] items = new ItemStack[INVENTORY_SIZE];
    private ItemStack cursorItem = ItemStack.AIR_ITEM;

    private InventoryCondition inventoryCondition;
    private InventoryClickProcessor clickProcessor = new InventoryClickProcessor();

    public PlayerInventory(Player player) {
        this.player = player;

        Arrays.fill(items, ItemStack.AIR_ITEM);
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return this.items[slot];
    }

    @Override
    public ItemStack[] getItemStacks() {
        return Arrays.copyOf(items, items.length);
    }

    @Override
    public void setInventoryRule() {

    }

    @Override
    public InventoryCondition getInventoryCondition() {
        return inventoryCondition;
    }

    @Override
    public void setInventoryCondition(InventoryCondition inventoryCondition) {
        this.inventoryCondition = inventoryCondition;
    }

    @Override
    public void setItemStack(int slot, ItemStack itemStack) {
        safeItemInsert(slot, itemStack);
    }

    @Override
    public boolean addItemStack(ItemStack itemStack) {
        synchronized (this) {
            StackingRule stackingRule = itemStack.getStackingRule();
            for (int i = 0; i < items.length - 10; i++) {
                ItemStack item = items[i];
                StackingRule itemStackingRule = item.getStackingRule();
                if (item.isAir()) {
                    setItemStack(i, itemStack);
                    return true;
                } else if (itemStack.isSimilar(item)) {
                    int itemAmount = item.getAmount();
                    if (itemAmount == stackingRule.getMaxSize())
                        continue;
                    int totalAmount = itemStack.getAmount() + itemAmount;
                    if (!stackingRule.canApply(itemStack, totalAmount)) {
                        item = itemStackingRule.apply(item, itemStackingRule.getMaxSize());

                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        itemStack = stackingRule.apply(itemStack, totalAmount - stackingRule.getMaxSize());
                    } else {
                        item.setAmount((byte) totalAmount);
                        sendSlotRefresh((short) convertToPacketSlot(i), item);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ItemStack getItemInMainHand() {
        return getItemStack(player.getHeldSlot());
    }

    public void setItemInMainHand(ItemStack itemStack) {
        safeItemInsert(player.getHeldSlot(), itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    public ItemStack getItemInOffHand() {
        return getItemStack(OFFHAND_SLOT);
    }

    public void setItemInOffHand(ItemStack itemStack) {
        safeItemInsert(OFFHAND_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    public ItemStack getHelmet() {
        return getItemStack(HELMET_SLOT);
    }

    public void setHelmet(ItemStack itemStack) {
        safeItemInsert(HELMET_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    public ItemStack getChestplate() {
        return getItemStack(CHESTPLATE_SLOT);
    }

    public void setChestplate(ItemStack itemStack) {
        safeItemInsert(CHESTPLATE_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    public ItemStack getLeggings() {
        return getItemStack(LEGGINGS_SLOT);
    }

    public void setLeggings(ItemStack itemStack) {
        safeItemInsert(LEGGINGS_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    public ItemStack getBoots() {
        return getItemStack(BOOTS_SLOT);
    }

    public void setBoots(ItemStack itemStack) {
        safeItemInsert(BOOTS_SLOT, itemStack);
        player.syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
    }

    public void update() {
        PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(createWindowItemsPacket());
    }

    public void refreshSlot(int slot) {
        sendSlotRefresh((short) convertToPacketSlot(slot), getItemStack(slot));
    }

    public ItemStack getCursorItem() {
        return cursorItem;
    }

    public void setCursorItem(ItemStack cursorItem) {
        this.cursorItem = cursorItem;
    }

    public ItemStack getEquipment(EntityEquipmentPacket.Slot slot) {
        switch (slot) {
            case MAIN_HAND:
                return getItemInMainHand();
            case OFF_HAND:
                return getItemInOffHand();
            case HELMET:
                return getHelmet();
            case CHESTPLATE:
                return getChestplate();
            case LEGGINGS:
                return getLeggings();
            case BOOTS:
                return getBoots();
        }
        return ItemStack.AIR_ITEM;
    }

    private void safeItemInsert(int slot, ItemStack itemStack) {
        synchronized (this) {
            itemStack = itemStack == null ? ItemStack.AIR_ITEM : itemStack;
            this.items[slot] = itemStack;
            // System.out.println("INSERT: " + slot);
            //sendSlotRefresh((short) slot, itemStack);
            update();
            if (slot == player.getHeldSlot()) {
                player.syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
            } else if (slot == OFFHAND_SLOT) {
                player.syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
            } else if (slot == HELMET_SLOT) {
                player.syncEquipment(EntityEquipmentPacket.Slot.HELMET);
            } else if (slot == CHESTPLATE_SLOT) {
                player.syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
            } else if (slot == LEGGINGS_SLOT) {
                player.syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
            } else if (slot == BOOTS_SLOT) {
                player.syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
            }
        }
    }

    protected void setItemStack(int slot, int offset, ItemStack itemStack) {
        slot = convertSlot(slot, offset);
        safeItemInsert(slot, itemStack);
    }

    protected ItemStack getItemStack(int slot, int offset) {
        slot = convertSlot(slot, offset);
        return this.items[slot];
    }

    protected int convertToPacketSlot(int slot) {
        if (slot > -1 && slot < 9) { // Held bar 0-9
            slot = slot + 36;
        } else if (slot > 8 && slot < 36) { // Inventory 9-35
            slot = slot;
        } else if (slot >= CRAFT_SLOT_1 && slot <= CRAFT_RESULT) { // Crafting 36-40
            slot = slot - 36;
        } else if (slot >= HELMET_SLOT && slot <= BOOTS_SLOT) { // Armor 41-44
            slot = slot - 36;
        } else if (slot == OFFHAND_SLOT) { // Off hand
            slot = 45;
        }
        return slot;
    }

    private void sendSlotRefresh(short slot, ItemStack itemStack) {
        SetSlotPacket setSlotPacket = new SetSlotPacket();
        setSlotPacket.windowId = (byte) (slot > 35 && slot < INVENTORY_SIZE ? 0 : -2);
        setSlotPacket.slot = slot;
        setSlotPacket.itemStack = itemStack;
        player.getPlayerConnection().sendPacket(setSlotPacket);
    }

    private WindowItemsPacket createWindowItemsPacket() {
        ItemStack[] convertedSlots = new ItemStack[INVENTORY_SIZE];

        for (int i = 0; i < items.length; i++) {
            int slot = convertToPacketSlot(i);
            convertedSlots[slot] = items[i];
        }

        WindowItemsPacket windowItemsPacket = new WindowItemsPacket();
        windowItemsPacket.windowId = 0;
        windowItemsPacket.count = INVENTORY_SIZE;
        windowItemsPacket.items = convertedSlots;
        return windowItemsPacket;
    }

    private Map<Player, Set<Integer>> leftDraggingMap = new HashMap<>();
    private Map<Player, Set<Integer>> rightDraggingMap = new HashMap<>();

    @Override
    public void leftClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(convertSlot(slot, OFFSET));

        InventoryClickResult clickResult = clickProcessor.leftClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
    }

    @Override
    public void rightClick(Player player, int slot) {
        ItemStack cursor = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.rightClick(getInventoryCondition(), player, slot, clicked, cursor);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setCursorItem(clickResult.getCursor());
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
    public void shiftClick(Player player, int slot) {
        ItemStack clicked = getItemStack(slot, OFFSET);
        ItemStack cursorItem = getCursorItem(); // Not used

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                setItemStack(slot, OFFSET, clicked);
                setCursorItem(cursorItem);
                // Refresh client slot
                sendSlotRefresh((short) slot, clicked);
                return;
            }
        }
        // End condition

        if (clicked.isAir())
            return;

        StackingRule clickedRule = clicked.getStackingRule();

        ItemStack resultClicked = clicked.clone();
        boolean filled = false;
        for (int i = 0; i < items.length; i++) {
            int index = i < 9 ? i + 9 : i - 9;
            ItemStack item = items[index];
            StackingRule itemRule = item.getStackingRule();
            if (item.isSimilar(clicked)) {
                int amount = item.getAmount();
                if (!clickedRule.canApply(clicked, amount + 1))
                    continue;
                int totalAmount = resultClicked.getAmount() + amount;
                if (!clickedRule.canApply(clicked, totalAmount)) {
                    item = itemRule.apply(item, itemRule.getMaxSize());
                    setItemStack(index, OFFSET, item);

                    resultClicked = clickedRule.apply(resultClicked, totalAmount - clickedRule.getMaxSize());
                    filled = false;
                    continue;
                } else {
                    resultClicked = clickedRule.apply(resultClicked, totalAmount);
                    setItemStack(index, resultClicked);

                    item = itemRule.apply(item, 0);
                    setItemStack(slot, OFFSET, item);
                    filled = true;
                    break;
                }
            } else if (item.isAir()) {
                // Switch
                setItemStack(index, resultClicked);
                setItemStack(slot, OFFSET, ItemStack.AIR_ITEM);
                filled = true;
                break;
            }
        }
        if (!filled) {
            setItemStack(slot, OFFSET, resultClicked);
        }
    }

    @Override
    public void changeHeld(Player player, int slot, int key) {
        if (!getCursorItem().isAir())
            return;

        ItemStack heldItem = getItemStack(key);
        ItemStack clicked = getItemStack(slot, OFFSET);

        InventoryClickResult clickResult = clickProcessor.changeHeld(getInventoryCondition(), player, slot, clicked, heldItem);

        if (clickResult.doRefresh())
            sendSlotRefresh((short) slot, clicked);

        setItemStack(slot, OFFSET, clickResult.getClicked());
        setItemStack(key, clickResult.getCursor());
    }

    @Override
    public void dragging(Player player, int slot, int button) {
        ItemStack cursorItem = getCursorItem();
        ItemStack clicked = null;
        if (slot != -999)
            clicked = getItemStack(slot, OFFSET);

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                setItemStack(slot, OFFSET, clicked);
                setCursorItem(cursorItem);
                // Refresh client slot
                sendSlotRefresh((short) slot, clicked);
                return;
            }
        }
        // End condition

        StackingRule stackingRule = cursorItem.getStackingRule();

        if (slot == -999) {
            // Start or end left/right drag
            if (button == 0) {
                // Start left
                this.leftDraggingMap.put(player, new HashSet<>());
            } else if (button == 4) {
                // Start right
                this.rightDraggingMap.put(player, new HashSet<>());
            } else if (button == 2) {
                // End left
                if (!leftDraggingMap.containsKey(player))
                    return;
                Set<Integer> slots = leftDraggingMap.get(player);
                int slotCount = slots.size();
                int cursorAmount = stackingRule.getAmount(cursorItem);
                if (slotCount > cursorAmount)
                    return;
                // Should be size of each defined slot (if not full)
                int slotSize = (int) ((float) cursorAmount / (float) slotCount);
                int finalCursorAmount = cursorAmount;

                for (Integer s : slots) {
                    ItemStack draggedItem = cursorItem.clone();
                    ItemStack slotItem = getItemStack(s, OFFSET);
                    int maxSize = stackingRule.getMaxSize();
                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        int amount = slotItem.getAmount() + slotSize;
                        if (stackingRule.canApply(slotItem, amount)) {
                            slotItem = stackingRule.apply(slotItem, amount);
                            finalCursorAmount -= slotSize;
                        } else {
                            int removedAmount = amount - maxSize;
                            slotItem = stackingRule.apply(slotItem, maxSize);
                            finalCursorAmount -= removedAmount;
                        }
                    } else if (slotItem.isAir()) {
                        slotItem = stackingRule.apply(draggedItem, slotSize);
                        finalCursorAmount -= slotSize;
                    }

                    setItemStack(s, OFFSET, slotItem);
                }
                cursorItem = stackingRule.apply(cursorItem, finalCursorAmount);
                setCursorItem(cursorItem);

                leftDraggingMap.remove(player);
            } else if (button == 6) {
                // End right
                if (!rightDraggingMap.containsKey(player))
                    return;
                Set<Integer> slots = rightDraggingMap.get(player);
                int size = slots.size();
                int cursorAmount = stackingRule.getAmount(cursorItem);
                if (size > cursorAmount)
                    return;
                for (Integer s : slots) {
                    ItemStack draggedItem = cursorItem.clone();
                    ItemStack slotItem = getItemStack(s, OFFSET);
                    if (stackingRule.canBeStacked(draggedItem, slotItem)) {
                        int amount = slotItem.getAmount() + 1;
                        if (stackingRule.canApply(slotItem, amount)) {
                            slotItem = stackingRule.apply(slotItem, amount);
                            setItemStack(s, OFFSET, slotItem);
                        }
                    } else if (slotItem.isAir()) {
                        draggedItem = stackingRule.apply(draggedItem, 1);
                        setItemStack(s, OFFSET, draggedItem);
                    }
                }
                cursorItem = stackingRule.apply(cursorItem, cursorAmount - size);
                setCursorItem(cursorItem);

                rightDraggingMap.remove(player);

            }
        } else {
            // Add slot
            if (button == 1) {
                // Add left slot
                if (!leftDraggingMap.containsKey(player))
                    return;
                leftDraggingMap.get(player).add(slot);

            } else if (button == 5) {
                // Add right slot
                if (!rightDraggingMap.containsKey(player))
                    return;
                rightDraggingMap.get(player).add(slot);
            }
        }

    }

    @Override
    public void doubleClick(Player player, int slot) {
        ItemStack cursorItem = getCursorItem();
        ItemStack clicked = getItemStack(slot, OFFSET);

        // Start condition
        InventoryCondition inventoryCondition = getInventoryCondition();
        if (inventoryCondition != null) {
            InventoryConditionResult result = new InventoryConditionResult(clicked, cursorItem);
            inventoryCondition.accept(player, slot, result);

            cursorItem = result.getCursorItem();
            clicked = result.getClickedItem();

            if (result.isCancel()) {
                setItemStack(slot, OFFSET, clicked);
                setCursorItem(cursorItem);
                // Refresh client slot
                sendSlotRefresh((short) slot, clicked);
                return;
            }
        }
        // End condition

        if (cursorItem.isAir())
            return;

        StackingRule cursorRule = cursorItem.getStackingRule();
        int amount = cursorItem.getAmount();

        if (!cursorRule.canApply(cursorItem, amount + 1))
            return;

        for (int i = 0; i < items.length; i++) {
            int index = i < 9 ? i + 9 : i - 9;
            if (index == slot)
                continue;
            ItemStack item = items[index];
            StackingRule itemRule = item.getStackingRule();
            if (!cursorRule.canApply(cursorItem, amount + 1))
                break;
            if (cursorRule.canBeStacked(cursorItem, item)) {
                int totalAmount = amount + item.getAmount();
                if (!cursorRule.canApply(cursorItem, totalAmount)) {
                    cursorItem = cursorRule.apply(cursorItem, cursorRule.getMaxSize());

                    item = itemRule.apply(item, totalAmount - itemRule.getMaxSize());
                    setItemStack(index, item);
                } else {
                    cursorItem = cursorRule.apply(cursorItem, totalAmount);
                    item = itemRule.apply(item, 0);
                    setItemStack(index, item);
                }
                amount = cursorItem.getAmount();
            }
        }

        setCursorItem(cursorItem);
    }
}
