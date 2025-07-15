package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jspecify.annotations.Nullable;

public class EndCrystalMeta extends EntityMeta {
    public EndCrystalMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @Nullable Point getBeamTarget() {
        return metadata.get(MetadataDef.EndCrystal.BEAM_TARGET);
    }

    public void setBeamTarget(@Nullable Point value) {
        metadata.set(MetadataDef.EndCrystal.BEAM_TARGET, value);
    }

    public boolean isShowingBottom() {
        return metadata.get(MetadataDef.EndCrystal.SHOW_BOTTOM);
    }

    public void setShowingBottom(boolean value) {
        metadata.set(MetadataDef.EndCrystal.SHOW_BOTTOM, value);
    }

}
