package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractVillagerMeta extends AgeableMobMeta {
    protected AbstractVillagerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getHeadShakeTimer() {
        return metadata.get(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER);
    }

    public void setHeadShakeTimer(int value) {
        metadata.set(MetadataDef.AbstractVillager.HEAD_SHAKE_TIMER, value);
    }
}
