package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.Nullable;

public final class EndCrystalMeta extends EntityMeta {
    public EndCrystalMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @Nullable Point getBeamTarget() {
        return get(MetadataDef.EndCrystal.BEAM_TARGET);
    }

    public void setBeamTarget(@Nullable Point value) {
        set(MetadataDef.EndCrystal.BEAM_TARGET, value);
    }

    public boolean isShowingBottom() {
        return get(MetadataDef.EndCrystal.SHOW_BOTTOM);
    }

    public void setShowingBottom(boolean value) {
        set(MetadataDef.EndCrystal.SHOW_BOTTOM, value);
    }

}
