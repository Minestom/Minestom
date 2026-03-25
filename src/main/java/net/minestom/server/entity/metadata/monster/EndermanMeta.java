package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public final class EndermanMeta extends MonsterMeta {
    public EndermanMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @Nullable Block getCarriedBlock() {
        return get(MetadataDef.Enderman.CARRIED_BLOCK);
    }

    public void setCarriedBlock(@Nullable Block value) {
        set(MetadataDef.Enderman.CARRIED_BLOCK, value);
    }

    public boolean isScreaming() {
        return get(MetadataDef.Enderman.IS_SCREAMING);
    }

    public void setScreaming(boolean value) {
        set(MetadataDef.Enderman.IS_SCREAMING, value);
    }

    public boolean isStaring() {
        return get(MetadataDef.Enderman.IS_STARING);
    }

    public void setStaring(boolean value) {
        set(MetadataDef.Enderman.IS_STARING, value);
    }

}
