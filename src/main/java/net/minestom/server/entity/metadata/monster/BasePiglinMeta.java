package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BasePiglinMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    protected BasePiglinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setImmuneToZombification(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

}
