package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.inventory.click.ClickHandler;
import net.minestom.server.inventory.click.ClickPreprocessor;
import net.minestom.server.inventory.click.StandardClickHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends AbstractInventory {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_SIZE = 36;

    public static final int HELMET_SLOT = 5;
    public static final int CHESTPLATE_SLOT = 6;
    public static final int LEGGINGS_SLOT = 7;
    public static final int BOOTS_SLOT = 8;
    public static final int OFFHAND_SLOT = 45;

    public static final int HOTBAR_START = 36;

    public static final @NotNull ClickHandler clickHandler = new StandardClickHandler(
            (player, inventory, item, slot) -> {
                slot = Math.abs(slot);

                IntIterator base = IntIterators.EMPTY_ITERATOR;

                var equipmentSlot = item.material().registry().equipmentSlot();
                if (equipmentSlot != null && slot != equipmentSlot.armorSlot()) {
                    base = IntIterators.concat(base, IntIterators.singleton(equipmentSlot.armorSlot()));
                }

                if (item.material() == Material.SHIELD && slot != OFFHAND_SLOT) {
                    base = IntIterators.concat(base, IntIterators.singleton(OFFHAND_SLOT));
                }

                if (slot < 9 || slot > 35) {
                    base = IntIterators.concat(base, IntIterators.fromTo(9, 36));
                }

                if (slot < 36 || slot > 44) {
                    base = IntIterators.concat(base, IntIterators.fromTo(36, 45));
                }

                if (slot == 0) {
                    base = IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(base)));
                }

                return base;
            },
            (player, inventory, item, slot) -> IntIterators.fromTo(1, inventory.getSize())
    );

    protected final ClickPreprocessor clickPreprocessor = new ClickPreprocessor(this);

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public synchronized void clear() {
        super.clear();

        // Update equipment
        for (var player : getViewers()) {
            player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
        }
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
    public void refreshSlot(int slot, @NotNull ItemStack itemStack) {
        super.refreshSlot(slot, itemStack);

        for (var player : getViewers()) {
            var equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            player.syncEquipment(equipmentSlot, itemStack);
        }
    }

    private int getSlotIndex(@NotNull EquipmentSlot slot, int heldSlot) {
        return switch (slot) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> slot.armorSlot();
            case OFF_HAND -> OFFHAND_SLOT;
            case MAIN_HAND -> HOTBAR_START + heldSlot;
        };
    }

    private @Nullable EquipmentSlot fromSlotIndex(int slot, int heldSlot) {
        return switch (slot) {
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case OFFHAND_SLOT -> EquipmentSlot.OFF_HAND;
            default -> slot == (HOTBAR_START + heldSlot) ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot, int heldSlot) {
        return getItemStack(getSlotIndex(slot, heldSlot));
    }

    public void setEquipment(@NotNull EquipmentSlot slot, int heldSlot, @NotNull ItemStack newValue) {
        setItemStack(getSlotIndex(slot, heldSlot), newValue);
    }

    @Override
    public byte getWindowId() {
        return 0;
    }

    /**
     * Refreshes an inventory slot.
     *
     * @param slot      the packet slot
     * @param itemStack the item stack in the slot
     */
    protected void sendSlotRefresh(short slot, ItemStack itemStack) {
        SetSlotPacket defaultPacket = new SetSlotPacket((byte) 0, 0, slot, itemStack);

        for (Player viewer : getViewers()) {
            var openInventory = player.getOpenInventory();
            if (openInventory != null && slot >= OFFSET && slot < OFFSET + INNER_INVENTORY_SIZE) {
                this.player.sendPacket(new SetSlotPacket(openInventory.getWindowId(), 0, (short) (slot + openInventory.getSize() - OFFSET), itemStack));
            } else if (openInventory == null || slot == OFFHAND_SLOT) {
                this.player.sendPacket(defaultPacket);
            }
        }
    }

    @Override
    public @NotNull ClickPreprocessor getClickPreprocessor() {
        return clickPreprocessor;
    }

    @Override
    public @NotNull ClickHandler getClickHandler() {
        return clickHandler;
    }

    public static @NotNull IntIterator getInnerShiftClickSlots(@NotNull Player player, @NotNull ItemStack item, int slot) {
        return IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(IntIterators.fromTo(-44, -10))));
    }

    public static @NotNull IntIterator getInnerDoubleClickSlots(@NotNull Player player, @NotNull ItemStack item, int slot) {
        return IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(IntIterators.fromTo(-44, -10))));
    }
}
