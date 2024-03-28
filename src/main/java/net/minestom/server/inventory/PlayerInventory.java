package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.ClickHandler;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.inventory.click.StandardClickHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends InventoryImpl {

    public static final @NotNull ClickHandler CLICK_HANDLER = new StandardClickHandler(
            (builder, item, slot) -> {
                IntIterator base = IntIterators.EMPTY_ITERATOR;

                var equipmentSlot = item.material().registry().equipmentSlot();
                if (equipmentSlot != null && slot != equipmentSlot.armorSlot()) {
                    base = IntIterators.concat(base, IntIterators.singleton(equipmentSlot.armorSlot()));
                }

                if (item.material() == Material.SHIELD && slot != OFF_HAND_SLOT) {
                    base = IntIterators.concat(base, IntIterators.singleton(OFF_HAND_SLOT));
                }

                if (slot < 9 || slot > 35) {
                    base = IntIterators.concat(base, IntIterators.fromTo(9, 36));
                }

                if (slot < 0 || slot > 8) {
                    base = IntIterators.concat(base, IntIterators.fromTo(0, 9));
                }

                if (slot == CRAFT_RESULT) {
                    base = IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(base)));
                }

                return IntIterators.pour(base);
            },
            (builder, item, slot) -> IntIterators.pour(IntIterators.concat(
                    IntIterators.fromTo(CRAFT_SLOT_1, CRAFT_SLOT_4 + 1), // 1-4
                    IntIterators.fromTo(HELMET_SLOT, BOOTS_SLOT + 1), // 5-8
                    IntIterators.fromTo(9, 36), // 9-35
                    IntIterators.fromTo(0, 9), // 36-44
                    IntIterators.singleton(OFF_HAND_SLOT) // 45
            ))
    );

    public static @NotNull IntList getInnerShiftClickSlots(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot) {
        return IntIterators.pour(IntIterators.fromTo(builder.clickedInventory().getSize(), builder.clickedInventory().getSize() + 36));
    }

    public static @NotNull IntList getInnerDoubleClickSlots(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot) {
        return IntIterators.pour(IntIterators.fromTo(builder.clickedInventory().getSize(), builder.clickedInventory().getSize() + 36));
    }

    private static int getSlotIndex(@NotNull EquipmentSlot slot, int heldSlot) {
        return switch (slot) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> slot.armorSlot();
            case OFF_HAND -> OFF_HAND_SLOT;
            case MAIN_HAND -> heldSlot;
        };
    }

    private static @Nullable EquipmentSlot fromSlotIndex(int slot, int heldSlot) {
        return switch (slot) {
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case OFF_HAND_SLOT -> EquipmentSlot.OFF_HAND;
            default -> slot == heldSlot ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    private static final IntList FILL_ADD_SLOTS = IntIterators.pour(IntIterators.concat(
        IntIterators.singleton(OFF_HAND_SLOT),
        IntIterators.fromTo(0, 36)
    ));

    private static final IntList AIR_ADD_SLOTS = IntIterators.pour(IntIterators.fromTo(0, 36));

    private static final IntList TAKE_SLOTS = IntIterators.pour(IntIterators.concat(
        IntIterators.fromTo(0, 36),
        IntIterators.singleton(OFF_HAND_SLOT),
        IntIterators.fromTo(36, 45)
    ));

    private ItemStack cursorItem = ItemStack.AIR;

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public byte getWindowId() {
        return 0;
    }

    /**
     * Gets the cursor item of this inventory
     * @return the cursor item that is shared between all viewers
     */
    public @NotNull ItemStack getCursorItem() {
        return cursorItem;
    }

    /**
     * Sets the cursor item for all viewers of this inventory.
     * @param cursorItem the new item (will not update if same as current)
     */
    public void setCursorItem(@NotNull ItemStack cursorItem) {
        setCursorItem(cursorItem, true);
    }

    /**
     * Sets the cursor item for all viewers of this inventory.
     * @param cursorItem the new item (will not update if same as current)
     * @param sendPacket whether or not to send a packet
     */
    public void setCursorItem(@NotNull ItemStack cursorItem, boolean sendPacket) {
        if (this.cursorItem.equals(cursorItem)) return;

        lock.lock();
        try {
            this.cursorItem = cursorItem;
        } finally {
            lock.unlock();
        }

        if (!sendPacket) return;
        sendPacketToViewers(SetSlotPacket.createCursorPacket(cursorItem));
    }

    @Override
    public void updateSlot(int slot, @NotNull ItemStack itemStack) {
        SetSlotPacket defaultPacket = new SetSlotPacket(getWindowId(), 0, (short) PlayerInventoryUtils.minestomToProtocol(slot), itemStack);

        for (Player player : getViewers()) {
            Inventory open = player.getOpenInventory();
            if (open != null && slot >= 0 && slot < INNER_SIZE) {
                player.sendPacket(new SetSlotPacket(open.getWindowId(), 0, (short) PlayerInventoryUtils.minestomToProtocol(slot, open), itemStack));
            } else if (open == null || slot == OFF_HAND_SLOT) {
                player.sendPacket(defaultPacket);
            }

            var equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            player.syncEquipment(equipmentSlot, itemStack);
        }
    }

    @Override
    public void update(@NotNull Player player) {
        ItemStack[] local = getItemStacks();
        ItemStack[] mapped = new ItemStack[getSize()];

        for (int slot = 0; slot < getSize(); slot++) {
            mapped[PlayerInventoryUtils.minestomToProtocol(slot)] = local[slot];
        }

        player.sendPacket(new WindowItemsPacket(getWindowId(), 0, List.of(mapped), getCursorItem()));
    }

    @Override
    protected void UNSAFE_itemInsert(int slot, @NotNull ItemStack itemStack) {
        for (var player : getViewers()) {
            final EquipmentSlot equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            EntityEquipEvent entityEquipEvent = new EntityEquipEvent(player, itemStack, equipmentSlot);
            EventDispatcher.call(entityEquipEvent);
            itemStack = entityEquipEvent.getEquippedItem();
        }

        super.UNSAFE_itemInsert(slot, itemStack);
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            super.clear();

            for (var player : getViewers()) {
                player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @Nullable ClickResult handleClick(@NotNull Player player, @NotNull ClickInfo clickInfo) {
        return CLICK_HANDLER.handleClick(this, player, clickInfo);
    }

    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot, int heldSlot) {
        return getItemStack(getSlotIndex(slot, heldSlot));
    }

    public void setEquipment(@NotNull EquipmentSlot slot, int heldSlot, @NotNull ItemStack newValue) {
        setItemStack(getSlotIndex(slot, heldSlot), newValue);
    }

    @Override
    public <T> @NotNull T addItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.add(FILL_ADD_SLOTS, AIR_ADD_SLOTS), option);
    }

    @Override
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(TAKE_SLOTS), option);
    }

}
