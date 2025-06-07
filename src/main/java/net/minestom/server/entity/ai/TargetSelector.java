package net.minestom.server.entity.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The target selector is called each time the entity receives an "attack" instruction
 * without having a target.
 */
public interface TargetSelector {

    /**
     * Finds the target.
     * <p>
     * Returning null means that this target selector didn't find any entity,
     * the next {@link TargetSelector} will be called until the end of the list or an entity is found.
     *
     * @return the target, null if not any
     */
    @Nullable
    Entity findTargetEntity(@NotNull EntityCreature entityCreature);

    /**
     * Finds the target position
     * <p>
     * Returning null means that this target selector is not meant to find positions, or did not find a valid positions, and the next {@link TargetSelector} will be called until the end of the list or a valid position is found.
     * @return The position found, if valid
     */
    @Nullable
    Pos findTargetPosition(@NotNull EntityCreature entityCreature);
}
