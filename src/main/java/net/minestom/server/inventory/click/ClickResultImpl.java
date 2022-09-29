package net.minestom.server.inventory.click;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

class ClickResultImpl {
    private ClickResultImpl() {
    }

    record Single(@NotNull ItemStack remaining,
                  @NotNull Map<Integer, ItemStack> changedSlots,
                  boolean requireUpdate) implements ClickResult.Single {
        static @NotNull Single empty() {
            return new ClickResultImpl.Single(ItemStack.AIR, Map.of());
        }

        public Single {
            changedSlots = Map.copyOf(changedSlots);
        }

        public Single(@NotNull ItemStack remaining,
                      @NotNull Map<Integer, ItemStack> changedSlots) {
            this(remaining, changedSlots, false);
        }
    }

    record Double(@NotNull ItemStack remaining,
                  @NotNull Map<Integer, ItemStack> playerChanges,
                  @NotNull Map<Integer, ItemStack> inventoryChanges,
                  boolean requireUpdate) implements ClickResult.Double {
        static @NotNull Double empty() {
            return new ClickResultImpl.Double(ItemStack.AIR, Map.of(), Map.of());
        }

        public Double {
            playerChanges = Map.copyOf(playerChanges);
            inventoryChanges = Map.copyOf(inventoryChanges);
        }

        public Double(@NotNull ItemStack remaining,
                      @NotNull Map<Integer, ItemStack> playerChanges,
                      @NotNull Map<Integer, ItemStack> inventoryChanges) {
            this(remaining, playerChanges, inventoryChanges, false);
        }
    }

    record Drop(@NotNull ItemStack remaining,
                @NotNull ItemStack drop,
                boolean requireUpdate) implements ClickResult.Drop {
        public Drop(@NotNull ItemStack remaining,
                    @NotNull ItemStack drop) {
            this(remaining, drop, false);
        }
    }
}
