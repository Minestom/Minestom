package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractVillagerMeta extends AgeableMobMeta {

    protected AbstractVillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getHeadShakeTimer() {
        return super.metadata.getIndex((byte) 16, 0);
    }

    public void setHeadShakeTimer(int value) {
        super.metadata.setIndex((byte) 16, Metadata.VarInt(value));
    }
}
