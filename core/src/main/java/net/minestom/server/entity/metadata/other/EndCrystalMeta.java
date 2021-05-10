package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndCrystalMeta extends EntityMeta {

    public EndCrystalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Nullable
    public BlockPosition getBeamTarget() {
        return super.metadata.getIndex((byte) 7, null);
    }

    public void setBeamTarget(@Nullable BlockPosition value) {
        super.metadata.setIndex((byte) 7, Metadata.OptPosition(value));
    }

    public boolean isShowingBottom() {
        return super.metadata.getIndex((byte) 8, true);
    }

    public void setShowingBottom(boolean value) {
        super.metadata.setIndex((byte) 8, Metadata.Boolean(value));
    }

}
