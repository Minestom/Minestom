package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event when the item usage duration has passed for a player, meaning when the item has completed its usage.
 */
public class ItemUsageCompleteEvent implements PlayerInstanceEvent, ItemEvent {

    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;

    public ItemUsageCompleteEvent(@NotNull Player player, @NotNull PlayerHand hand,
                                  @NotNull ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    @NotNull
    public PlayerHand getHand() {
        return hand;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
