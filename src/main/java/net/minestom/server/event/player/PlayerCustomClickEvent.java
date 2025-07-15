package net.minestom.server.event.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jspecify.annotations.Nullable;

/**
 * Triggered when we receive a custom click packet from the client during the <b>play</b> state.
 *
 * @see PlayerConfigCustomClickEvent
 */
public class PlayerCustomClickEvent implements PlayerInstanceEvent {
    private final Player player;
    private final Key key;
    private final BinaryTag payload;

    public PlayerCustomClickEvent(Player player, Key key, @Nullable BinaryTag payload) {
        this.player = player;
        this.key = key;
        this.payload = payload;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public Key getKey() {
        return this.key;
    }

    public @Nullable BinaryTag getPayload() {
        return this.payload;
    }

}
