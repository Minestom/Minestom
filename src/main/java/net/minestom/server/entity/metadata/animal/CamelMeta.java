package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class CamelMeta extends AbstractHorseMeta {
    public CamelMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDashing() {
        return metadata.get(MetadataDef.Camel.DASHING);
    }

    public void setDashing(boolean value) {
        metadata.set(MetadataDef.Camel.DASHING, value);
    }

    public long getLastPoseChangeTick() {
        return metadata.get(MetadataDef.Camel.LAST_POSE_CHANGE_TICK);
    }

    public void setLastPoseChangeTick(long value) {
        metadata.set(MetadataDef.Camel.LAST_POSE_CHANGE_TICK, value);
    }
}
