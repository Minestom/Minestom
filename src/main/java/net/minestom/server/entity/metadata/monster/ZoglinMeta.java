package net.minestom.server.entity.metadata.monster;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AgeableMeta;
import org.jetbrains.annotations.NotNull;

public class ZoglinMeta extends MonsterMeta implements AgeableMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public ZoglinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Override
    public boolean isBaby() {
        return super.metadata.getIndex(OFFSET, false);
    }

    @Override
    public void setBaby(boolean value) {
        if (isBaby() == value) return;
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getEntityType().registry().boundingBox();
            if (value) entity.setBoundingBox(bb.width() * 0.5, bb.height() * 0.5, bb.depth() * 0.5);
            else entity.setBoundingBox(bb);
        });
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

}
