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
public record EntityTeleportEvent(@NotNull Entity entity, @NotNull Pos teleportPosition, @MagicConstant(flagsFromClass = RelativeFlags.class) int relativeFlags) implements EntityEvent {

    /**
     * @return The {@link Entity} that teleported.
     */
    @NotNull
    @Override
    public Entity entity() {
        return entity;
    }

    /**
     * @return Computes the position that the {@link Entity} is about to teleport to. This is an absolute position.
     */
    public @NotNull Pos newPosition() {
        return PositionUtils.getPositionWithRelativeFlags(this.entity().getPosition(), teleportPosition(), relativeFlags);
    }

    /**
     * @return The position that the {@link Entity} is about to teleport to. This may be (partially) relative depending on the flags.
     */
    @Override
    public @NotNull Pos teleportPosition() {
        return teleportPosition;
    }

    /**
     * @return The flags that determine which fields of the position are relative.
     */
    @Override
    @MagicConstant(flagsFromClass = RelativeFlags.class)
    public int relativeFlags() {
        return relativeFlags;
    }
}
