package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event when a player updates an item state, meaning when they stop using the item.
 * This event is also called when the item usage duration has passed, in which case {@link #isCompleted()} returns true.
 */
public class ItemUpdateStateEvent implements PlayerInstanceEvent, ItemEvent {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;
    private final boolean completed;

    private boolean handAnimation;
    private boolean riptideSpinAttack;

    public ItemUpdateStateEvent(@NotNull Player player, @NotNull Player.Hand hand,
                                @NotNull ItemStack itemStack, boolean completed) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.completed = completed;
    }

    @NotNull
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Gets whether the item usage is completed. This is the case if the item usage duration has passed.
     *
     * @return whether the item usage is completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets whether the player should have a hand animation.
     *
     * @param handAnimation whether the player should have a hand animation
     */
    public void setHandAnimation(boolean handAnimation) {
        this.handAnimation = handAnimation;
    }

    public boolean hasHandAnimation() {
        return handAnimation;
    }

    /**
     * Sets whether the player should have a riptide spin attack animation.
     *
     * @param riptideSpinAttack whether the player should have a riptide spin attack animation
     */
    public void setRiptideSpinAttack(boolean riptideSpinAttack) {
        this.riptideSpinAttack = riptideSpinAttack;
    }

    public boolean isRiptideSpinAttack() {
        return riptideSpinAttack;
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
