package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops using an item before the item has completed its usage, including the amount of
 * time the item was used before cancellation.
 *
 * <p>This includes cases like half eating a food, but also includes shooting a bow.</p>
 */
public class PlayerCancelItemUseEvent implements PlayerInstanceEvent, ItemEvent {
    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;
    private final long useDuration;
    private boolean isRiptideSpinAttack = false;

    public PlayerCancelItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long useDuration) {
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
