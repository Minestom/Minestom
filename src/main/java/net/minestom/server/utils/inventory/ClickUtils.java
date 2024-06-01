package net.minestom.server.utils.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ClickUtils {

    /**
     * Gets the item that was clicked.
     *
     * For most clicks, this is straightforward; left clicks return the item in the slot, and drop clicks return the
     * dropped item. Drag clicks return the cursor item, too, and hotbar swaps return the initial slot that was clicked
     * (may not be a hotbar slot).
     */
    public static @NotNull ItemStack getItem(Click.Info info, Inventory inventory, PlayerInventory playerInventory) {
        int raw = ClickUtils.getSlot(info);

        int slot = PlayerInventoryUtils.protocolToMinestom(raw, inventory.getSize());
        if (slot == -1) {
            return info instanceof Click.Info.CreativeDropItem(ItemStack item) ? item :
                    playerInventory.getCursorItem();
        } else {
            Inventory clicked = (inventory instanceof PlayerInventory || (raw != -1 && raw >= inventory.getSize()))
                     ? playerInventory : inventory;

            return clicked.getItemStack(slot);
        }
    }

    /**
     * Gets the slot from click info.
     *
     * If there is no slot, or there are arbitrarily many slots, -1 is returned.
     */
    public static int getSlot(@NotNull Click.Info info) {
        return switch (info) {
            // Single-slot clicks
            case Click.Info.Left(int slot) -> slot;
            case Click.Info.Right(int slot) -> slot;
            case Click.Info.Middle(int slot) -> slot;
            case Click.Info.LeftShift(int slot) -> slot;
            case Click.Info.RightShift(int slot) -> slot;
            case Click.Info.Double(int slot) -> slot;
            case Click.Info.DropSlot(int slot, boolean ignored) -> slot;
            case Click.Info.OffhandSwap(int slot) -> slot;
            case Click.Info.CreativeSetItem(int slot, ItemStack ignored) -> slot;

            // Zero-slot clicks
            case Click.Info.LeftDropCursor() -> -1;
            case Click.Info.RightDropCursor() -> -1;
            case Click.Info.MiddleDropCursor() -> -1;
            case Click.Info.CreativeDropItem(ItemStack ignored) -> -1;

            // Multi-slot clicks
            case Click.Info.HotbarSwap(int hotbarSlot, int clickedSlot) -> clickedSlot;
            case Click.Info.LeftDrag(List<Integer> slots) -> -1;
            case Click.Info.RightDrag(List<Integer> slots) -> -1;
            case Click.Info.MiddleDrag(List<Integer> slots) -> -1;
        };
    }

    /**
     * Converts click info into its respective type. This is a simple 1:1 mapping.
     */
    public static @NotNull Click.Type getType(@NotNull Click.Info info) {
        return switch (info) {
            case Click.Info.CreativeDropItem ignored -> Click.Type.CREATIVE_DROP_ITEM;
            case Click.Info.CreativeSetItem ignored -> Click.Type.CREATIVE_SET_ITEM;
            case Click.Info.Double ignored -> Click.Type.DOUBLE;
            case Click.Info.DropSlot ignored -> Click.Type.DROP_SLOT;
            case Click.Info.HotbarSwap ignored -> Click.Type.HOTBAR_SWAP;
            case Click.Info.Left ignored -> Click.Type.LEFT;
            case Click.Info.LeftDrag ignored -> Click.Type.LEFT_DRAG;
            case Click.Info.LeftDropCursor ignored -> Click.Type.LEFT_DROP_CURSOR;
            case Click.Info.LeftShift ignored -> Click.Type.LEFT_SHIFT;
            case Click.Info.Middle ignored -> Click.Type.MIDDLE;
            case Click.Info.MiddleDrag ignored -> Click.Type.MIDDLE_DRAG;
            case Click.Info.MiddleDropCursor ignored -> Click.Type.MIDDLE_DROP_CURSOR;
            case Click.Info.OffhandSwap ignored -> Click.Type.OFFHAND_SWAP;
            case Click.Info.Right ignored -> Click.Type.RIGHT;
            case Click.Info.RightDrag ignored -> Click.Type.RIGHT_DRAG;
            case Click.Info.RightDropCursor ignored -> Click.Type.RIGHT_DROP_CURSOR;
            case Click.Info.RightShift ignored -> Click.Type.RIGHT_SHIFT;
        };
    }

    public static @NotNull Click.Getter makeGetter(@NotNull Inventory inventory, @NotNull PlayerInventory playerInventory) {
        return new Click.Getter(inventory::getItemStack, playerInventory::getItemStack, playerInventory.getCursorItem(), inventory.getSize());
    }

    /**
     * Determines whether or not the given changes are conservative (i.e., whether or not they create or delete items).
     */
    public static boolean conservative(@NotNull List<Click.Change> clientDefault, @NotNull Inventory inventory, @NotNull PlayerInventory playerInventory) {
        Click.Getter getter = makeGetter(inventory, playerInventory);
        return consolidate(clientDefault, getter.mainSize()).conservative(getter);
    }

    /**
     * Converts a click window packet and a click info into a list of changes.
     */
    public static @NotNull List<Click.Change> packetToChanges(@NotNull ClientClickWindowPacket packet, @NotNull Click.Info info, @NotNull Click.Getter getter, boolean playerInventory) {
        List<Click.Change> changes = new ArrayList<>();

        for (var change : packet.changedSlots()) {
            int slot = change.slot();
            if (playerInventory) slot = PlayerInventoryUtils.protocolToMinestom(slot);
            changes.add(new Click.Change.Container(slot, change.item()));
        }

        changes.add(new Click.Change.Cursor(packet.clickedItem()));

        if (info instanceof Click.Info.OffhandSwap swap && !playerInventory) {
            changes.add(new Click.Change.Player(PlayerInventoryUtils.OFF_HAND_SLOT, getter.get(swap.slot())));
        }

        switch (info) {
            case Click.Info.LeftDropCursor() -> changes.add(new Click.Change.DropFromPlayer(getter.cursor()));
            case Click.Info.RightDropCursor() -> changes.add(new Click.Change.DropFromPlayer(getter.cursor().withAmount(1)));
            case Click.Info.DropSlot(int slot, boolean all) -> changes.add(new Click.Change.DropFromPlayer(all ? getter.get(slot) : getter.get(slot).withAmount(1)));
            case Click.Info.CreativeDropItem(ItemStack item) -> changes.add(new Click.Change.DropFromPlayer(item));
            default -> {}
        }

        return changes;
    }

    /**
     * Consolidates a list of changes into a single object.
     */
    public static @NotNull FlatChanges consolidate(@NotNull List<Click.Change> changes, int size) {
        Map<Integer, ItemStack> container = new HashMap<>();
        Map<Integer, ItemStack> player = new HashMap<>();
        @Nullable ItemStack cursor = null;
        List<ItemStack> dropped = new ArrayList<>();

        for (var change : changes) {
            switch (change) {
                case Click.Change.Container(int slot, ItemStack item) -> {
                    if (slot < size) {
                        container.put(slot, item);
                    } else {
                        player.put(PlayerInventoryUtils.protocolToMinestom(slot, size), item);
                    }
                }
                case Click.Change.Player(int slot, ItemStack item) -> player.put(slot, item);
                case Click.Change.Cursor(ItemStack item) -> cursor = item;
                case Click.Change.DropFromPlayer(ItemStack item) -> dropped.add(item);
            }
        }

        return new FlatChanges(container, player, cursor, dropped);
    }

    public record FlatChanges(@NotNull Map<Integer, ItemStack> container, @NotNull Map<Integer, ItemStack> player,
                              @Nullable ItemStack cursor, @NotNull List<ItemStack> dropped) {

        /**
         * Determines whether or not these changes are conservative. If they create or destroy any items, the changes
         * are not conservative.
         */
        public boolean conservative(@NotNull Click.Getter getter) {
            // A count of each item; this will ideally end up with everything at zero.
            Object2IntMap<ItemStack> items = new Object2IntOpenHashMap<>();
            items.defaultReturnValue(0);

            BiConsumer<ItemStack, Integer> updater = (item, count) -> {
                if (item.isAir()) return;

                ItemStack one = item.withAmount(1);
                items.put(one, items.getInt(one) + count);
            };

            container.values().forEach(item -> updater.accept(item, item.amount()));
            player.values().forEach(item -> updater.accept(item, item.amount()));
            dropped.forEach(item -> updater.accept(item, item.amount()));

            container.keySet().stream().map(getter.main()::apply).forEach(item -> updater.accept(item, -item.amount()));
            player.keySet().stream().map(getter.player()::apply).forEach(item -> updater.accept(item, -item.amount()));

            if (cursor != null) {
                updater.accept(cursor, cursor.amount());
                updater.accept(getter.cursor(), -getter.cursor().amount());
            }

            return items.values().intStream().allMatch(i -> i == 0);
        }

    }

}
