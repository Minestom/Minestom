package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Stores changes that occurred or will occur as the result of a click.
 * @param player the player who clicked in the inventory
 * @param clickedInventory the clicked inventory. This may be the player's inventory
 * @param changes the map of changes that will occur to the inventory
 * @param playerInventoryChanges the map of changes that will occur to the player inventory
 * @param newCursorItem the player's cursor item after this click. Null indicates no change
 * @param sideEffects the side effects of this click
 */
public record ClickResult(@NotNull Player player, @NotNull Inventory clickedInventory,
                          @NotNull Map<Integer, ItemStack> changes, @NotNull Map<Integer, ItemStack> playerInventoryChanges,
                          @Nullable ItemStack newCursorItem, @Nullable SideEffects sideEffects) {

    public static @NotNull Builder builder(@NotNull Player player, @NotNull Inventory clickedInventory) {
        return new Builder(player, clickedInventory);
    }

    public static final class Builder {
        private final @NotNull Player player;
        private final @NotNull Inventory clickedInventory;

        private final Int2ObjectMap<ItemStack> changes = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<ItemStack> playerInventoryChanges = new Int2ObjectArrayMap<>();
        private @Nullable ItemStack newCursorItem;
        private @Nullable SideEffects sideEffects;

        Builder(@NotNull Player player, @NotNull Inventory clickedInventory) {
            this.player = player;
            this.clickedInventory = clickedInventory;
        }

        public @NotNull Player player() {
            return player;
        }

        public @NotNull Inventory clickedInventory() {
            return clickedInventory;
        }

        public @NotNull PlayerInventory playerInventory() {
            return player().getInventory();
        }

        public @NotNull ItemStack getCursorItem() {
            return player.getCursorItem();
        }

        public @NotNull ItemStack get(int slot) {
            if (slot >= clickedInventory.getSize()) {
                return playerInventory().getItemStack(PlayerInventoryUtils.protocolToMinestom(slot - clickedInventory.getSize() + 9));
            } else {
                return clickedInventory.getItemStack(slot);
            }
        }

        public @NotNull Builder change(int slot, @NotNull ItemStack item) {
            if (slot >= clickedInventory.getSize()) {
                change(PlayerInventoryUtils.protocolToMinestom(slot - clickedInventory.getSize() + 9), item, true);
            } else {
                change(slot, item, false);
            }
            return this;
        }

        public @NotNull Builder change(int slot, @NotNull ItemStack item, boolean playerInventory) {
            (playerInventory ? playerInventoryChanges : changes).put(slot, item);
            return this;
        }

        public @NotNull Builder cursor(@Nullable ItemStack newCursorItem) {
            this.newCursorItem = newCursorItem;
            return this;
        }

        public @NotNull Builder sideEffects(@Nullable SideEffects sideEffects) {
            this.sideEffects = sideEffects;
            return this;
        }

        public @NotNull ClickResult build() {
            return new ClickResult(
                    player, clickedInventory,
                    Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(changes)),
                    Int2ObjectMaps.unmodifiable(new Int2ObjectArrayMap<>(playerInventoryChanges)),
                    newCursorItem, sideEffects
            );
        }

    }

    /**
     * Applies the changes of this result to the player and the clicked inventory.
     * @param player the player who clicked
     * @param clickedInventory the inventory that was clicked in
     */
    public void applyChanges(@NotNull Player player, @NotNull Inventory clickedInventory) {
        for (var entry : changes.entrySet()) {
            clickedInventory.setItemStack(entry.getKey(), entry.getValue());
        }

        for (var entry : playerInventoryChanges.entrySet()) {
            player.getInventory().setItemStack(entry.getKey(), entry.getValue());
        }

        if (newCursorItem != null) {
            player.setCursorItem(newCursorItem);
        }

        if (sideEffects != null) {
            sideEffects.apply(player, clickedInventory);
        }
    }

    /**
     * Represents side effects that may occur as the result of an inventory click.
     */
    public interface SideEffects {

        /**
         * A side effect that results in the player dropping an item.
         * @param item the dropped item
         */
        record DropFromPlayer(@NotNull ItemStack item) implements SideEffects {
            @Override
            public void apply(@NotNull Player player, @NotNull Inventory clickedInventory) {
                player.dropItem(item);
            }
        }

        /**
         * Applies these side effects to the provided player and their open inventory.
         * @param player the player who clicked
         * @param clickedInventory the clicked inventory
         */
        void apply(@NotNull Player player, @NotNull Inventory clickedInventory);
    }
}
