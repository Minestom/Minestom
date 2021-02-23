package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractVillagerMeta extends AgeableMobMeta {

    protected AbstractVillagerMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getHeadShakeTimer() {
        return getMetadata().getIndex((byte) 16, 0);
    }

    public void setHeadShakeTimer(int value) {
        getMetadata().setIndex((byte) 16, Metadata.VarInt(value));
    }
}
