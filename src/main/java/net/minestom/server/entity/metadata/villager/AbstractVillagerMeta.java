package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;

public sealed abstract class AbstractVillagerMeta extends AgeableMobMeta permits VillagerMeta {
    protected AbstractVillagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getHeadShakeTimer() {
        return get(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER);
    }

    public void setHeadShakeTimer(int value) {
        set(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER, value);
    }
}
