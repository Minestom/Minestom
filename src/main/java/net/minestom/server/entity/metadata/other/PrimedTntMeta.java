package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class PrimedTntMeta extends EntityMeta {

    public PrimedTntMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getFuseTime() {
        return super.metadata.getIndex((byte) 7, 80);
    }

    public void setFuseTime(int value) {
        super.metadata.setIndex((byte) 7, Metadata.VarInt(value));
    }

}
