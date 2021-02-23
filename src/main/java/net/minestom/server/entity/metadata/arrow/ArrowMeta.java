package net.minestom.server.entity.metadata.arrow;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import org.jetbrains.annotations.NotNull;

public class ArrowMeta extends BaseEntityMeta {

    public ArrowMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return super.metadata.getIndex((byte) 9, -1);
    }

    public void setColor(int value) {
        super.metadata.setIndex((byte) 9, Metadata.VarInt(value));
    }

}
