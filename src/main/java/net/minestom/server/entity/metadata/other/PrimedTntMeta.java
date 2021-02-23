package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import org.jetbrains.annotations.NotNull;

public class PrimedTntMeta extends BaseEntityMeta {

    public PrimedTntMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getFuseTime() {
        return getMetadata().getIndex((byte) 7, 80);
    }

    public void setFuseTime(int value) {
        getMetadata().setIndex((byte) 7, Metadata.VarInt(value));
    }

}
