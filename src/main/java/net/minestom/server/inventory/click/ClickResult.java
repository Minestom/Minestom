package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores changes that occurred or will occur as the result of a click.
 * @param changes the map of changes that will occur to the inventory
 * @param playerInventoryChanges the map of changes that will occur to the player inventory
 * @param newCursorItem the player's cursor item after this click. Null indicates no change
 * @param sideEffects the side effects of this click
 */
public record ClickResult(@NotNull Int2ObjectMap<ItemStack> changes, @NotNull Int2ObjectMap<ItemStack> playerInventoryChanges,
                          @Nullable ItemStack newCursorItem, @Nullable SideEffects sideEffects) {

    public static @NotNull ClickResult empty() {
        return new ClickResult(Int2ObjectMaps.emptyMap(), Int2ObjectMaps.emptyMap(), null, null);
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Int2ObjectMap<ItemStack> changes = new Int2ObjectArrayMap<>();
        private final Int2ObjectMap<ItemStack> playerInventoryChanges = new Int2ObjectArrayMap<>();
        private @Nullable ItemStack newCursorItem;
        private @Nullable SideEffects sideEffects;

        public @NotNull Builder change(int slot, @NotNull ItemStack item) {
            change(Math.abs(slot), item, slot < 0);
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
    public void applyChanges(@NotNull Player player, @NotNull AbstractInventory clickedInventory) {
        for (var entry : changes.int2ObjectEntrySet()) {
            clickedInventory.setItemStack(entry.getIntKey(), entry.getValue());
        }

        for (var entry : playerInventoryChanges.int2ObjectEntrySet()) {
            player.getInventory().setItemStack(entry.getIntKey(), entry.getValue());
        }

        if (newCursorItem != null) {
            clickedInventory.setCursorItem(player, newCursorItem);
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
            public void apply(@NotNull Player player, @NotNull AbstractInventory clickedInventory) {
                player.dropItem(item);
            }
        }

        /**
         * Applies these side effects to the provided player and their open inventory.
         * @param player the player who clicked
         * @param clickedInventory the clicked inventory
         */
        void apply(@NotNull Player player, @NotNull AbstractInventory clickedInventory);
    }
}
