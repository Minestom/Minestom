package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketMeta extends EntityMeta implements ProjectileMeta {

    private Entity shooter;

    public FireworkRocketMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getFireworkInfo() {
        return super.metadata.getIndex((byte) 7, ItemStack.getAirItem());
    }

    public void setFireworkInfo(@NotNull ItemStack value) {
        super.metadata.setIndex((byte) 7, Metadata.Slot(value));
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
        super.metadata.setIndex((byte) 8, Metadata.OptVarInt(entityID));
    }

    public boolean isShotAtAngle() {
        return super.metadata.getIndex((byte) 9, false);
    }

    public void setShotAtAngle(boolean value) {
        super.metadata.setIndex((byte) 9, Metadata.Boolean(value));
    }

}
