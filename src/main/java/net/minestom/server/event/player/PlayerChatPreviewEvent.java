package net.minestom.server.event.player;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerChatPreviewEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private final int id;
    private final String query;
    private boolean cancelled;
    private @Nullable Component result;

    public PlayerChatPreviewEvent(Player player, int id, String query) {
        this.player = player;
        this.id = id;
        this.query = query;
    }

    public int getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public @Nullable Component getResult() {
        return result;
    }

    public void setResult(@Nullable Component result) {
        this.result = result;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
