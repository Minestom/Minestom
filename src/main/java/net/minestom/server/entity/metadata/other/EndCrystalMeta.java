package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndCrystalMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public EndCrystalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public @Nullable Point getBeamTarget() {
        return super.metadata.getIndex(OFFSET, null);
    }

    public void setBeamTarget(@Nullable Point value) {
        super.metadata.setIndex(OFFSET, Metadata.OptPosition(value));
    }

    public boolean isShowingBottom() {
        return super.metadata.getIndex(OFFSET + 1, true);
    }

    public void setShowingBottom(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

}
