package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PillagerMeta extends AbstractIllagerMeta {
    public static final byte OFFSET = AbstractIllagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public PillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    // Microtus start - meta update
    /**
     * Set the state if a Pillager charge his crossbow.
     * @param value the value to set
     */
    public void setChargingCrossbow(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    /**
     * Returns a boolean value if a Pillager is charging his crossbow.
     * @return true when yes otherwise false
     */
    public boolean isChargingCrossbow() {
        return super.metadata.getIndex(OFFSET, false);
    }
    // Microtus end - meta update
}
