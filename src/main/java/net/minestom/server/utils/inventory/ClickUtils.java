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
