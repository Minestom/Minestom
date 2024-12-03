package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndermanMeta extends MonsterMeta {
    public EndermanMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Integer getCarriedBlockID() {
        return metadata.get(MetadataDef.Enderman.CARRIED_BLOCK);
    }

    public void setCarriedBlockID(@Nullable Integer value) {
        metadata.set(MetadataDef.Enderman.CARRIED_BLOCK, value);
    }

    public boolean isScreaming() {
        return metadata.get(MetadataDef.Enderman.IS_SCREAMING);
    }

    public void setScreaming(boolean value) {
        metadata.set(MetadataDef.Enderman.IS_SCREAMING, value);
    }

    public boolean isStaring() {
        return metadata.get(MetadataDef.Enderman.IS_STARING);
    }

    public void setStaring(boolean value) {
        metadata.set(MetadataDef.Enderman.IS_STARING, value);
    }

}
