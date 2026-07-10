package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.Nullable;

public class AbstractVillagerMeta extends AgeableMobMeta {
    protected AbstractVillagerMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getHeadShakeTimer() {
        return metadata.get(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER);
    }

    public void setHeadShakeTimer(int value) {
        metadata.set(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER, value);
    }
}
