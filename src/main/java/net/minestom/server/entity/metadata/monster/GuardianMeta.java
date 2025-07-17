package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuardianMeta extends MonsterMeta {
    private Entity target;

    public GuardianMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isRetractingSpikes() {
        return metadata.get(MetadataDef.Guardian.IS_RETRACTING_SPIKES);
    }

    public void setRetractingSpikes(boolean value) {
        metadata.set(MetadataDef.Guardian.IS_RETRACTING_SPIKES, value);
    }

    public int getTargetEntityId() {
        return metadata.get(MetadataDef.Guardian.TARGET_EID);
    }

    @ApiStatus.Internal
    public void setTargetEntityId(int value) {
        metadata.set(MetadataDef.Guardian.TARGET_EID, value);
    }

    public Entity getTarget() {
        return this.target;
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
        setTargetEntityId(target == null ? 0 : target.getEntityId());
    }

}
