package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

// Microtus - entity meta update
/**
 * This metadata implementation can be used for a warden entity.
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 **/
public class WardenMeta extends MonsterMeta {

    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;


    /**
     * Creates a new metadata which can be set to a warden entity.
     * @param entity the warden entity reference
     * @param metadata the reference to a {@link Metadata}
     */
    public WardenMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * Set the anger level for a warden.
     * @param value the level to set
     */
    public void setAngerLevel(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    /**
     * Returns the given anger level from the meta or zero as default value if no value is set.
     * @return the given level
     */
    public int getAngerLevel() {
        return super.metadata.getIndex(OFFSET, 0);
    }
}
