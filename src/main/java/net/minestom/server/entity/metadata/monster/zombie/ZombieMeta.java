package net.minestom.server.entity.metadata.monster.zombie;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import org.jetbrains.annotations.NotNull;

public class ZombieMeta extends MonsterMeta {
    public ZombieMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isBaby() {
        return metadata.get(MetadataDef.Zombie.IS_BABY);
    }

    public void setBaby(boolean value) {
        if (isBaby() == value) {
            return;
        }
        this.consumeEntity((entity) -> {
            BoundingBox bb = entity.getBoundingBox();
            if (value) {
                double width = bb.width() / 2;
                entity.setBoundingBox(width, bb.height() / 2, width);
            } else {
                double width = bb.width() * 2;
                entity.setBoundingBox(width, bb.height() * 2, width);
            }
        });
        metadata.set(MetadataDef.Zombie.IS_BABY, value);
    }

    public boolean isBecomingDrowned() {
        return metadata.get(MetadataDef.Zombie.IS_BECOMING_DROWNED);
    }

    public void setBecomingDrowned(boolean value) {
        metadata.set(MetadataDef.Zombie.IS_BECOMING_DROWNED, value);
    }

}
