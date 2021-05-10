package net.minestom.server.entity.type.other;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.other.EndCrystalMeta} instead.
 */
@Deprecated
public class EntityEndCrystal extends ObjectEntity {

    public EntityEndCrystal(@NotNull Position spawnPosition) {
        super(EntityType.END_CRYSTAL, spawnPosition);

        setBoundingBox(2f, 2f, 2f);
    }

    @Nullable
    public BlockPosition getBeamTarget() {
        return metadata.getIndex((byte) 7, null);
    }

    public void setBeamTarget(@Nullable BlockPosition beamTarget) {
        this.metadata.setIndex((byte) 7, Metadata.OptPosition(beamTarget));
    }

    public boolean showBottom() {
        return metadata.getIndex((byte) 8, true);
    }

    public void setShowBottom(boolean showBottom) {
        this.metadata.setIndex((byte) 8, Metadata.Boolean(showBottom));
    }

    @Override
    public int getObjectData() {
        return 0;
    }
}
