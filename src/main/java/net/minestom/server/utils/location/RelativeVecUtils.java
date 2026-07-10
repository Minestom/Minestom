package net.minestom.server.utils.location;

import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Framework-side convenience helpers resolving a {@link RelativeVec} against live
 * {@link Entity}/{@link CommandSender} positions.
 * <p>
 * The pure {@link RelativeVec#from(Pos)}/{@link RelativeVec#fromView(Pos)} core stays on the record.
 */
public final class RelativeVecUtils {
    private RelativeVecUtils() {
    }

    /**
     * Gets the location based on the relative fields and the entity position.
     *
     * @param relativeVec the relative vector to resolve
     * @param entity      the entity to get the relative position from
     * @return the location
     */
    public static Vec from(RelativeVec relativeVec, @Nullable Entity entity) {
        return relativeVec.from(entity != null ? entity.getPosition() : Pos.ZERO);
    }

    /**
     * Shorthand for {@link RelativeVec#from(Pos)}.
     * If the sender is a player its position is used, otherwise {@link Pos#ZERO}.
     *
     * @param relativeVec the relative vector to resolve
     * @param sender      the command sender
     * @return the position with any relativity
     */
    public static Vec fromSender(RelativeVec relativeVec, @Nullable CommandSender sender) {
        final Pos position = sender instanceof Player player ? player.getPosition() : Pos.ZERO;
        return relativeVec.from(position);
    }

    /**
     * Shorthand for {@link RelativeVec#fromView(Pos)}.
     *
     * @param relativeVec the relative vector to resolve
     * @param entity      the entity to get the position from, otherwise {@link Pos#ZERO}
     * @return the view
     */
    public static Vec fromView(RelativeVec relativeVec, @Nullable Entity entity) {
        return relativeVec.fromView(entity != null ? entity.getPosition() : Pos.ZERO);
    }
}
