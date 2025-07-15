package net.minestom.server.entity.metadata.monster;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jspecify.annotations.Nullable;

public class CreakingMeta extends MonsterMeta {
    public CreakingMeta(Entity entity, MetadataHolder metadata) {
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

    public boolean isTearingDown() {
        return metadata.get(MetadataDef.Creaking.IS_TEARING_DOWN);
    }

    public void setTearingDown(boolean value) {
        metadata.set(MetadataDef.Creaking.IS_TEARING_DOWN, value);
    }

    public @Nullable Point getHomePos() {
        return metadata.get(MetadataDef.Creaking.HOME_POS);
    }

    public void setHomePos(@Nullable Point value) {
        metadata.set(MetadataDef.Creaking.HOME_POS, value);
    }
}
