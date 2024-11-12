package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class CreakingMeta extends MonsterMeta {
    public CreakingMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean canMove() {
        return metadata.get(MetadataDef.Creaking.CAN_MOVE);
    }

    public void setCanMove(boolean value) {
        metadata.set(MetadataDef.Creaking.CAN_MOVE, value);
    }

    public boolean isActive() {
        return metadata.get(MetadataDef.Creaking.IS_ACTIVE);
    }

    public void setActive(boolean value) {
        metadata.set(MetadataDef.Creaking.IS_ACTIVE, value);
    }
}
