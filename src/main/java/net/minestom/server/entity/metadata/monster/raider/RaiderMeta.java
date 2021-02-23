package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import org.jetbrains.annotations.NotNull;

public class RaiderMeta extends MonsterMeta {

    protected RaiderMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isCelebrating() {
        return getMetadata().getIndex((byte) 15, false);
    }

    public void setCelebrating(boolean value) {
        getMetadata().setIndex((byte) 15, Metadata.Boolean(value));
    }

}
