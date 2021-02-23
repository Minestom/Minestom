package net.minestom.server.entity.metadata.arrow;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import org.jetbrains.annotations.NotNull;

public class ArrowMeta extends BaseEntityMeta {

    public ArrowMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getColor() {
        return getMetadata().getIndex((byte) 9, -1);
    }

    public void setColor(int value) {
        getMetadata().setIndex((byte) 9, Metadata.VarInt(value));
    }

}
