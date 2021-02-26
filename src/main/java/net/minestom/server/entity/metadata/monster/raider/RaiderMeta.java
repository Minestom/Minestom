package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import org.jetbrains.annotations.NotNull;

public class RaiderMeta extends MonsterMeta {

    protected RaiderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isCelebrating() {
        return super.metadata.getIndex((byte) 15, false);
    }

    public void setCelebrating(boolean value) {
        super.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

}
