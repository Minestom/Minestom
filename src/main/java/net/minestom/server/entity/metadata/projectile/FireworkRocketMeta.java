package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketMeta extends EntityMeta implements ProjectileMeta {
    private Entity shooter;

    public FireworkRocketMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getFireworkInfo() {
        return metadata.get(MetadataDef.FireworkRocketEntity.ITEM);
    }

    public void setFireworkInfo(@NotNull ItemStack value) {
        metadata.set(MetadataDef.FireworkRocketEntity.ITEM, value);
    }

    @Nullable
    public Integer getShooterEntityId() {
        return metadata.get(MetadataDef.FireworkRocketEntity.SHOOTER_ENTITY_ID);
    }

    @ApiStatus.Internal
    public void setShooterEntityId(@Nullable Integer value) {
        metadata.set(MetadataDef.FireworkRocketEntity.SHOOTER_ENTITY_ID, value);
    }

    @Override
    @Nullable
    public Entity getShooter() {
        return this.shooter;
    }

    @Override
    public void setShooter(@Nullable Entity value) {
        this.shooter = value;
        Integer entityID = value == null ? null : value.getEntityId();
        setShooterEntityId(entityID);
    }

    public boolean isShotAtAngle() {
        return metadata.get(MetadataDef.FireworkRocketEntity.IS_SHOT_AT_ANGLE);
    }

    public void setShotAtAngle(boolean value) {
        metadata.set(MetadataDef.FireworkRocketEntity.IS_SHOT_AT_ANGLE, value);
    }

}
