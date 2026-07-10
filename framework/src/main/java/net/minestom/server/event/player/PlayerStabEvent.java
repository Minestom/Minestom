package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called when a player attempts to use a stab attack on an item with the {@link net.minestom.server.item.component.PiercingWeapon} enchantment.
 */
public class PlayerStabEvent implements PlayerInstanceEvent {
    private final Player player;

    public PlayerStabEvent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item which the player attacked with.
     *
     * @return the item in main hand
     */
    public ItemStack getItemStack() {
        return player.getItemInMainHand();
    }
}
