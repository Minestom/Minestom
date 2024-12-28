package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player completely finishes using an item.
 *
 * <p>{@link #getUseDuration()} represents the total time spent using the item.</p>
 */
public class PlayerFinishItemUseEvent implements PlayerInstanceEvent, ItemEvent {
    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;
    private final long useDuration;
    private boolean isRiptideSpinAttack = false;

    public PlayerFinishItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long useDuration) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.useDuration = useDuration;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull PlayerHand getHand() {
        return hand;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    public long getUseDuration() {
        return useDuration;
    }

    /**
     * True if this event will transition the player into a riptide spin attack.
     */
    public boolean isRiptideSpinAttack() {
        return isRiptideSpinAttack;
    }

    /**
     * True if this event will transition the player into a riptide spin attack.
     */
    public void setRiptideSpinAttack(boolean riptideSpinAttack) {
        isRiptideSpinAttack = riptideSpinAttack;
    }

}
