package net.minestom.server;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface Witness extends Viewable {

    /**
     * Gets if this entity is automatically sent to surrounding players.
     * True by default.
     *
     * @return true if the entity is automatically viewable for close players, false otherwise
     */
    boolean isAutoViewable();

    void setAutoViewable(boolean autoViewable);

    void updateViewableRule(@NotNull Predicate<Player> predicate);

    @ApiStatus.Experimental
    void updateViewableRule();

    /**
     * Gets if surrounding entities are automatically visible by this.
     * True by default.
     *
     * @return true if surrounding entities are visible by this
     */
    @ApiStatus.Experimental
    boolean autoViewEntities();

    /**
     * Decides if surrounding entities must be visible.
     *
     * @param autoViewer true to add view surrounding entities, false to remove
     */
    @ApiStatus.Experimental
    void setAutoViewEntities(boolean autoViewer);

    @ApiStatus.Experimental
    void updateViewerRule(@NotNull Predicate<Entity> predicate);

    @ApiStatus.Experimental
    void updateViewerRule();

    /**
     * Gets if this entity's viewers (surrounding players) can be predicted from surrounding chunks.
     */
    boolean hasPredictableViewers();

    @ApiStatus.Internal
    default void updateNewViewer(@NotNull Player player) {
        // Empty
    }

    @ApiStatus.Internal
    default void updateOldViewer(@NotNull Player player) {
        // Empty
    }
}
