package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WitherMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

    private Entity centerHead;
    private Entity leftHead;
    private Entity rightHead;

    public WitherMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Nullable
    public Entity getCenterHead() {
        return this.centerHead;
    }

    public void setCenterHead(@Nullable Entity value) {
        this.centerHead = value;
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value == null ? 0 : value.getEntityId()));
    }

    @Nullable
    public Entity getLeftHead() {
        return this.leftHead;
    }

    public void setLeftHead(@Nullable Entity value) {
        this.leftHead = value;
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value == null ? 0 : value.getEntityId()));
    }

    @Nullable
    public Entity getRightHead() {
        return this.rightHead;
    }

    public void setRightHead(@Nullable Entity value) {
        this.rightHead = value;
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value == null ? 0 : value.getEntityId()));
    }

    public int getInvulnerableTime() {
        return super.metadata.getIndex(OFFSET + 3, 0);
    }

    public void setInvulnerableTime(int value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value));
    }

}
