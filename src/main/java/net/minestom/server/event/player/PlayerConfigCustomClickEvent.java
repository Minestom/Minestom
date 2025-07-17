package net.minestom.server.event.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Triggered when we receive a custom click packet from the client during the <b>configuration</b> state.
 *
 * @see PlayerCustomClickEvent
 */
public class PlayerConfigCustomClickEvent implements PlayerEvent {
    private final Player player;
    private final Key key;
    private final BinaryTag payload;

    public PlayerConfigCustomClickEvent(@NotNull Player player, @NotNull Key key, @Nullable BinaryTag payload) {
        this.player = player;
        this.key = key;
        this.payload = payload;
    }

    @Override
    public @NotNull Player getPlayer() {
        return null;
    }

    public @NotNull Key getKey() {
        return this.key;
    }

    public @Nullable BinaryTag getPayload() {
        return this.payload;
    }
}
