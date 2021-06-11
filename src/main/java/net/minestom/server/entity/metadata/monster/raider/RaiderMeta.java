package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import org.jetbrains.annotations.NotNull;

public class RaiderMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected RaiderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isCelebrating() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setCelebrating(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

}
