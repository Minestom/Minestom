package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CamelMeta extends AbstractHorseMeta {
    public static final byte OFFSET = AbstractHorseMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public CamelMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isDashing() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setDashing(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    public long getLastPoseChangeTick() {
        return super.metadata.getIndex(OFFSET + 1, 0L);
    }

    public void setLastPoseChangeTick(long value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Long(value));
    }
}
