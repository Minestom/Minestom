package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SmallFireballMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta {
    private Entity shooter;

    public SmallFireballMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getItem() {
        return metadata.get(MetadataDef.SmartFireball.ITEM);
    }

    public void setItem(@NotNull ItemStack item) {
        metadata.set(MetadataDef.SmartFireball.ITEM, item);
    }

    @Override
    @Nullable
    public Entity getShooter() {
        return shooter;
    }

    @Override
    public void setShooter(@Nullable Entity shooter) {
        this.shooter = shooter;
    }

    @Override
    public int getObjectData() {
        return this.shooter == null ? 0 : this.shooter.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
