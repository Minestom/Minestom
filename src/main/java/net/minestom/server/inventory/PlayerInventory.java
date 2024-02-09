package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * Represents the inventory of a {@link Player}, retrieved with {@link Player#getInventory()}.
 */
public non-sealed class PlayerInventory extends InventoryImpl {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_SIZE = 36;

    public static final int HELMET_SLOT = 5;
    public static final int CHESTPLATE_SLOT = 6;
    public static final int LEGGINGS_SLOT = 7;
    public static final int BOOTS_SLOT = 8;
    public static final int OFF_HAND_SLOT = 45;

    public static final int HOTBAR_START = 36;

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

                if (slot < 36 || slot > 44) {
                    base = IntIterators.concat(base, IntIterators.fromTo(36, 45));
                }

                if (slot == 0) {
                    base = IntIterators.wrap(IntArrays.reverse(IntIterators.unwrap(base)));
                }

                return base;
            },
            (builder, item, slot) -> IntIterators.fromTo(1, builder.clickedInventory().getSize())
    );

    public static @NotNull IntIterator getInnerShiftClickSlots(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot) {
        return IntIterators.fromTo(builder.clickedInventory().getSize(), builder.clickedInventory().getSize() + 36);
    }

    public static @NotNull IntIterator getInnerDoubleClickSlots(@NotNull ClickResult.Builder builder, @NotNull ItemStack item, int slot) {
        return IntIterators.fromTo(builder.clickedInventory().getSize(), builder.clickedInventory().getSize() + 36);
    }

    private static int getSlotIndex(@NotNull EquipmentSlot slot, int heldSlot) {
        return switch (slot) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> slot.armorSlot();
            case OFF_HAND -> OFF_HAND_SLOT;
            case MAIN_HAND -> HOTBAR_START + heldSlot;
        };
    }

    private static @Nullable EquipmentSlot fromSlotIndex(int slot, int heldSlot) {
        return switch (slot) {
            case HELMET_SLOT -> EquipmentSlot.HELMET;
            case CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case OFF_HAND_SLOT -> EquipmentSlot.OFF_HAND;
            default -> slot == (HOTBAR_START + heldSlot) ? EquipmentSlot.MAIN_HAND : null;
        };
    }

    private static final int[] EXISTING_ADD_SLOTS = IntStream.concat(
            IntStream.concat(
                    IntStream.rangeClosed(36, 44),
                    IntStream.of(OFF_HAND_SLOT)
            ),
            IntStream.rangeClosed(9, 35)
    ).toArray();

    private static final int[] AIR_ADD_SLOTS = IntStream.concat(
            IntStream.rangeClosed(36, 44),
            IntStream.rangeClosed(9, 35)
    ).toArray();

    private static final int[] TAKE_SLOTS = IntStream.concat(
            IntStream.rangeClosed(36, 45),
            IntStream.concat(
                    IntStream.rangeClosed(9, 35),
                    IntStream.rangeClosed(0, 8)
            )
    ).toArray();

    public PlayerInventory() {
        super(INVENTORY_SIZE);
    }

    @Override
    public byte getWindowId() {
        return 0;
    }

    @Override
    public void updateSlot(int slot, @NotNull ItemStack itemStack) {
        super.updateSlot(slot, itemStack);

        for (var player : getViewers()) {
            var equipmentSlot = fromSlotIndex(slot, player.getHeldSlot());
            if (equipmentSlot == null) continue;

            player.syncEquipment(equipmentSlot, itemStack);
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
    public void clear() {
        lock.lock();
        try {
            super.clear();

            for (var player : getViewers()) {
                player.sendPacketToViewersAndSelf(player.getEquipmentsPacket());
            }
        } finally { lock.unlock(); }
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
        return processItemStack(itemStack, TransactionType.add(() -> IntIterators.wrap(EXISTING_ADD_SLOTS), () -> IntIterators.wrap(AIR_ADD_SLOTS)), option);
    }

    @Override
    public <T> @NotNull T takeItemStack(@NotNull ItemStack itemStack, @NotNull TransactionOption<T> option) {
        return processItemStack(itemStack, TransactionType.take(() -> IntIterators.wrap(TAKE_SLOTS)), option);
    }

}
