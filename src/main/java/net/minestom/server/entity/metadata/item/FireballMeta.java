package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FireballMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta {
    private MetaTarget shooter;

    public FireballMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public ItemStack getItem() {
        return metadata.get(MetadataDef.Fireball.ITEM);
    }

    public void setItem(ItemStack value) {
        metadata.set(MetadataDef.Fireball.ITEM, value);
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
