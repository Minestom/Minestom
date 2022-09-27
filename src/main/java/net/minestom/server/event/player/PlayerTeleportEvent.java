package net.minestom.server.event.player;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class PlayerTeleportEvent extends EntityTeleportEvent implements PlayerInstanceEvent {

    protected final Player player;
    
	public PlayerTeleportEvent(@NotNull Player player, @NotNull Pos position) {
		super(player, position);
		this.player = player;
	}

	@Override
	public @NotNull Player getPlayer() {
		return player;
	}

    @Override
    public @NotNull Player getEntity() {
        return player;
    }
}
