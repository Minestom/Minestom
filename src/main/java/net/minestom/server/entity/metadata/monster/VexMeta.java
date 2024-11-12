package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class VexMeta extends MonsterMeta {
    public VexMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isAttacking() {
        return metadata.get(MetadataDef.Vex.IS_ATTACKING);
    }

    public void setAttacking(boolean value) {
        metadata.set(MetadataDef.Vex.IS_ATTACKING, value);
    }

}
