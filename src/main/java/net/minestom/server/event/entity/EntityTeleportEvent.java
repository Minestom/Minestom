package net.minestom.server.event.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.utils.position.PositionUtils;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Called with {@link Entity#teleport(Pos)} and its overloads.
 */
public class EntityTeleportEvent implements EntityEvent {

    private final Entity entity;
    private final Pos teleportPosition;
    private final int relativeFlags;

    public EntityTeleportEvent(@NotNull Entity entity, @NotNull Pos teleportPosition, @MagicConstant(flagsFromClass = RelativeFlags.class) int relativeFlags) {
        this.entity = entity;
        this.teleportPosition = teleportPosition;
        this.relativeFlags = relativeFlags;
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
     * @return The position that the {@link Entity} is about to teleport to. This is an absolute position.
     */
    public @NotNull Pos getNewPosition() {
        return PositionUtils.getPositionWithRelativeFlags(this.getEntity().getPosition(), getTeleportPosition(), relativeFlags);
    }

    /**
     * @return The position that the {@link Entity} is about to teleport to. This may be (partially) relative depending on the flags.
     */
    public @NotNull Pos getTeleportPosition() {
        return teleportPosition;
    }

    /**
     * @return The flags that determine which fields of the position are relative.
     */
    @MagicConstant(flagsFromClass = RelativeFlags.class)
    public int getRelativeFlags() {
        return relativeFlags;
    }
}
