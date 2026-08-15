package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.utils.position.PositionUtils;
import org.intellij.lang.annotations.MagicConstant;

/**
 * Called with {@link Entity#teleport(Pos)} and its overloads.
 */
public class EntityTeleportEvent implements EntityEvent {

    private final Entity entity;
    private final Pos teleportPosition;
    private final int relativeFlags;

    public EntityTeleportEvent(Entity entity, Pos teleportPosition, @MagicConstant(flagsFromClass = RelativeFlags.class) int relativeFlags) {
        this.entity = entity;
        this.teleportPosition = teleportPosition;
        this.relativeFlags = relativeFlags;
    }

    /**
     * @return The {@link Entity} that teleported.
     */
    @Override
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the absolute position that the {@link Entity} is about to teleport to.
     *
     * @return the absolute teleport position
     */
    public Pos getNewPosition() {
        return PositionUtils.getPositionWithRelativeFlags(this.getEntity().getPosition(), getTeleportPosition(), relativeFlags);
    }

    /**
     * Gets the raw position that the {@link Entity} is about to teleport to,
     * which may be (partially) relative depending on the flags.
     *
     * @return the raw teleport position
     */
    public Pos getTeleportPosition() {
        return teleportPosition;
    }

    /**
     * {@return the flags that determine which fields of the position are relative}
     */
    @MagicConstant(flagsFromClass = RelativeFlags.class)
    public int getRelativeFlags() {
        return relativeFlags;
    }
}
