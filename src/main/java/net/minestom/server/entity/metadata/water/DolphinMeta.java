package net.minestom.server.entity.metadata.water;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class DolphinMeta extends WaterAnimalMeta {
    public static final byte OFFSET = WaterAnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    /**
     * Creates a new meta data for a dolphin.
     * @param entity the involved entity
     * @param metadata the base metadata
     */
    public DolphinMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * Get the given destination point for a treasure.
     * @return the point or {@link Vec#ZERO} when no point is set
     */
    public @NotNull Point getTreasurePosition() {
        return super.metadata.getIndex(OFFSET, Vec.ZERO);
    }

    /**
     * Set the destination point to lead a player to a treasure.
     * @param value the point to set
     */
    public void setTreasurePosition(@NotNull Point value) {
        super.metadata.setIndex(OFFSET, Metadata.BlockPosition(value));
    }

    /**
     * Returns an indicator if a Dolphin received a fish from a player.
     * @return true when yes otherwise false
     */
    @Deprecated
    public boolean hasFish() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    /**
     * Returns an indicator if a Dolphin received a fish from a player.
     * @return true when yes otherwise false
     */
    public boolean isHasFish() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    /**
     * Set the indicator if a Dolphin got a fish from a player.
     * @param value the value to set
     */
    public void setHasFish(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    /**
     * Get the current moisture level.
     * @return the given level
     */
    public int getMoistureLevel() {
        return super.metadata.getIndex(OFFSET + 2, 2400);
    }

    /**
     * Updates the given moisture level.
     * @param level the level to set
     */
    public void setMoistureLevel(int level) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(level));
    }

}
