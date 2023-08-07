package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WardenMeta extends MonsterMeta {

    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public WardenMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getAngerLevel() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setAngerLevel(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

}
