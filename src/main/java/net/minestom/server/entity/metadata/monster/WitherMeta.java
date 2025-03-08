package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class WitherMeta extends MonsterMeta {
    private WeakReference<Entity> centerHeadRef;
    private WeakReference<Entity> leftHeadRef;
    private WeakReference<Entity> rightHeadRef;

    public WitherMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getCenterHeadEntityId() {
        return metadata.get(MetadataDef.Wither.CENTER_HEAD_TARGET);
    }

    @ApiStatus.Internal
    public void setCenterHeadEntityId(int value) {
        metadata.set(MetadataDef.Wither.CENTER_HEAD_TARGET, value);
    }

    @Nullable
    public Entity getCenterHead() {
        return unwrap(centerHeadRef);
    }

    public void setCenterHead(@Nullable Entity value) {
        this.centerHeadRef = wrap(value);
        setCenterHeadEntityId(value == null ? 0 : value.getEntityId());
    }

    public int getLeftHeadEntityId() {
        return metadata.get(MetadataDef.Wither.LEFT_HEAD_TARGET);
    }

    @ApiStatus.Internal
    public void setLeftHeadEntityId(int value) {
        metadata.set(MetadataDef.Wither.LEFT_HEAD_TARGET, value);
    }

    @Nullable
    public Entity getLeftHead() {
        return unwrap(this.leftHeadRef);
    }

    public void setLeftHead(@Nullable Entity value) {
        this.leftHeadRef = wrap(value);
        setLeftHeadEntityId(value == null ? 0 : value.getEntityId());
    }

    public int getRightHeadEntityId() {
        return metadata.get(MetadataDef.Wither.RIGHT_HEAD_TARGET);
    }

    @ApiStatus.Internal
    public void setRightHeadEntityId(int value) {
        metadata.set(MetadataDef.Wither.RIGHT_HEAD_TARGET, value);
    }

    @Nullable
    public Entity getRightHead() {
        return unwrap(this.rightHeadRef);
    }

    public void setRightHead(@Nullable Entity value) {
        this.rightHeadRef = wrap(value);
        setRightHeadEntityId(value == null ? 0 : value.getEntityId());
    }

    public int getInvulnerableTime() {
        return metadata.get(MetadataDef.Wither.INVULNERABLE_TIME);
    }

    public void setInvulnerableTime(int value) {
        metadata.set(MetadataDef.Wither.INVULNERABLE_TIME, value);
    }

}
