package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SmallFireballMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta {
    private @Nullable MetaTarget shooter;

    public SmallFireballMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.SmartFireball.ITEM);
    }

    public void setItem(ItemStack item) {
        metadata.set(MetadataDef.SmartFireball.ITEM, item);
    }

    @Override
    @Nullable
    public MetaTarget getShooter() {
        return shooter;
    }

    @Override
    public void setShooter(@Nullable MetaTarget shooter) {
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
