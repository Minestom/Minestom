package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.MobMeta;
import org.jetbrains.annotations.NotNull;

public class SlimeMeta extends MobMeta {

    public SlimeMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getSize() {
        return getMetadata().getIndex((byte) 15, 0);
    }

    public void setSize(int value) {
        float boxSize = 0.51000005f * value;
        setBoundingBox(boxSize, boxSize);
        getMetadata().setIndex((byte) 15, Metadata.VarInt(value));
    }

}
