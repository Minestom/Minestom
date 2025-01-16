package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops using an item before the item has completed its usage, including the amount of
 * time the item was used before cancellation.
 *
 * <p>This includes cases like half eating a food, but also includes shooting a bow.</p>
 */
public record PlayerCancelItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long useDuration, boolean riptideSpinAttack) implements PlayerInstanceEvent, ItemEvent, MutableEvent<PlayerCancelItemUseEvent> {

    public PlayerCancelItemUseEvent(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long useDuration) {
        this(player, hand, itemStack, useDuration, false);
    }

    @Override
    public @NotNull Player player() {
        return player;
    }

    public @NotNull PlayerHand getHand() {
        return hand;
    }

    @Override
    public @NotNull ItemStack itemStack() {
        return itemStack;
    }

    public long getUseDuration() {
        return useDuration;
    }

    /**
     * True if this event will transition the player into a riptide spin attack.
     */
    @Override
    public boolean riptideSpinAttack() {
        return riptideSpinAttack;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutator<PlayerCancelItemUseEvent> {
        private final Player player;
        private final PlayerHand hand;
        private final ItemStack itemStack;
        private final long useDuration;
        private boolean riptideSpinAttack;

        public Mutator(PlayerCancelItemUseEvent event) {
            this.player = event.player;
            this.hand = event.hand;
            this.itemStack = event.itemStack;
            this.useDuration = event.useDuration;
            this.riptideSpinAttack = event.riptideSpinAttack;
        }

        /**
         * True if this event will transition the player into a riptide spin attack.
         */
        public boolean isRiptideSpinAttack() {
            return riptideSpinAttack;
        }

        /**
         * True if this event will transition the player into a riptide spin attack.
         */
        public void setRiptideSpinAttack(boolean riptideSpinAttack) {
            this.riptideSpinAttack = riptideSpinAttack;
        }

        @Override
        public @NotNull PlayerCancelItemUseEvent mutated() {
            return new PlayerCancelItemUseEvent(this.player, this.hand, this.itemStack, this.useDuration, this.riptideSpinAttack);
        }
    }
}
