package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ApiStatus.Internal
public interface ClickResult {

    boolean requireUpdate();

    /**
     * Result affecting a single inventory.
     */
    interface Single extends ClickResult {
        @NotNull ItemStack remaining();

        @NotNull Map<Integer, ItemStack> changedSlots();
    }

    interface Double extends ClickResult {
        @NotNull ItemStack remaining();

        @NotNull Map<Integer, ItemStack> playerChanges();

        @NotNull Map<Integer, ItemStack> inventoryChanges();
    }

    interface Drop extends ClickResult {
        @NotNull ItemStack remaining();

        @NotNull ItemStack drop();
    }
}
