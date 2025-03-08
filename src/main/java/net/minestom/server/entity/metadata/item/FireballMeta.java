package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.entity.metadata.projectile.ProjectileEntityMeta;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireballMeta extends ProjectileEntityMeta implements ObjectDataProvider {

    public FireballMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getItem() {
        return metadata.get(MetadataDef.Fireball.ITEM);
    }

    public void setItem(@NotNull ItemStack value) {
        metadata.set(MetadataDef.Fireball.ITEM, value);
    }

    @Override
    public int getObjectData() {
        final var shooter = getShooter();

        return shooter == null ? 0 : shooter.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
