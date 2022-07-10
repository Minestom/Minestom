package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemUpdateStateEvent implements PlayerInstanceEvent, ItemEvent {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;
    private boolean handAnimation;

    public ItemUpdateStateEvent(@NotNull Player player, @NotNull Player.Hand hand, @NotNull ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    @NotNull
    public Player.Hand getHand() {
        return hand;
    }

    public void setHandAnimation(boolean handAnimation) {
        this.handAnimation = handAnimation;
    }

    public boolean hasHandAnimation() {
        return handAnimation;
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
