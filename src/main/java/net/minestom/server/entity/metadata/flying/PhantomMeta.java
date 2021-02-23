package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PhantomMeta extends FlyingMeta {

    public PhantomMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getSize() {
        return getMetadata().getIndex((byte) 15, 0);
    }

    public void setSize(int value) {
        getMetadata().setIndex((byte) 15, Metadata.VarInt(value));
    }

}
