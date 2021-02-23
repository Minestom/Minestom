package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class BasePiglinMeta extends MonsterMeta {

    protected BasePiglinMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return super.metadata.getIndex((byte) 15, false);
    }

    public void setImmuneToZombification(boolean value) {
        super.metadata.setIndex((byte) 15, Metadata.Boolean(value));
    }

}
