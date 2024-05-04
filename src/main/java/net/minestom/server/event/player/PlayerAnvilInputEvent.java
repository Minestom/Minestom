package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.network.packet.client.play.ClientNameItemPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Called every time a {@link Player} types a letter in an anvil GUI.
 *
 * @see ClientNameItemPacket
 */
public class PlayerAnvilInputEvent implements PlayerInstanceEvent {

    private final Player player;
    private final String input;

    public PlayerAnvilInputEvent(@NotNull Player player, @NotNull String input) {
        this.player = player;
        this.input = input;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull String getInput() {
        return input;
    }
}
