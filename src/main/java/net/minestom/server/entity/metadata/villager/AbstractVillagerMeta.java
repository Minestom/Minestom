package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AbstractAgeableMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractVillagerMeta extends AbstractAgeableMeta {
    public static final byte OFFSET = AbstractAgeableMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected AbstractVillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getHeadShakeTimer() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setHeadShakeTimer(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }
}
