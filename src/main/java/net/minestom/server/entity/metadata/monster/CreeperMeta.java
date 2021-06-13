package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class CreeperMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public CreeperMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public State getState() {
        int id = super.metadata.getIndex(OFFSET, -1);
        return id == -1 ? State.IDLE : State.FUSE;
    }

    public void setState(@NotNull State value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value == State.IDLE ? -1 : 1));
    }

    public boolean isCharged() {
        return super.metadata.getIndex(OFFSET + 1, false);
    }

    public void setCharged(boolean value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Boolean(value));
    }

    public boolean isIgnited() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setIgnited(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

    public enum State {
        IDLE,
        FUSE
    }

}
