package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link Entity#teleport(Pos)} and its overloads.
 */
public class EntityTeleportEvent implements EntityEvent, CancellableEvent {

    private boolean cancelled;
    private final Entity entity;
    private final Pos oldPos;
    private Pos newPos;

    public EntityTeleportEvent(@NotNull Entity entity, @NotNull Pos oldPos, @NotNull Pos newPos) {
        this.entity = entity;
        this.oldPos = oldPos;
        this.newPos = newPos;
    }

    /**
     * @return The {@link Entity} that teleported.
     */
    @NotNull
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * @return The position that the {@link Entity} was at before they teleported.
     */
    @NotNull
    public Pos getOldPosition() {
        return oldPos;
    }

    /**
     * @return The position that the {@link Entity} is about to teleport to.
     */
    @NotNull
    public Pos getNewPosition() {
        return newPos;
    }

    /**
     * @param to The position that the {@link Entity} is teleporting to.
     */
    public void setNewPosition(@NotNull Pos to) {
        this.newPos = to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
