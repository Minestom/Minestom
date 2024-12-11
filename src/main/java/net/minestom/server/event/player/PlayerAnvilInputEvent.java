package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.network.packet.client.play.ClientNameItemPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Called every time a {@link Player} types a letter in an anvil GUI.
 *
 * @see ClientNameItemPacket
 */
public class PlayerAnvilInputEvent implements PlayerInstanceEvent, InventoryEvent {

    private final Player player;
    private final Inventory inventory;
    private final String input;

    public PlayerAnvilInputEvent(@NotNull Player player, @NotNull Inventory inventory, @NotNull String input) {
        this.player = player;
        this.inventory = inventory;
        this.input = input;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull String getInput() {
        return input;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
