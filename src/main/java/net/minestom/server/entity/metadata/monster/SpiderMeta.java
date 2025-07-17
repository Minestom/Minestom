package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class SpiderMeta extends MonsterMeta {
    public SpiderMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isClimbing() {
        return metadata.get(MetadataDef.Spider.IS_CLIMBING);
    }

    public void setClimbing(boolean value) {
        metadata.set(MetadataDef.Spider.IS_CLIMBING, value);
    }

}
