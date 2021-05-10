package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class PhantomMeta extends FlyingMeta {

    public PhantomMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getSize() {
        return super.metadata.getIndex((byte) 15, 0);
    }

    public void setSize(int value) {
        super.metadata.setIndex((byte) 15, Metadata.VarInt(value));
    }

}
