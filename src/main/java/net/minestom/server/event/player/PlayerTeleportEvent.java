package net.minestom.server.event.player;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class PlayerTeleportEvent extends EntityTeleportEvent implements PlayerInstanceEvent {

	public PlayerTeleportEvent(@NotNull Entity entity, @NotNull Pos position) {
		super(entity, position);
	}

	@Override
	public @NotNull Player getPlayer() {
		return (@NotNull Player) entity;
	}

    @Override
    public @NotNull Player getEntity() {
        return getPlayer();
    }
}
