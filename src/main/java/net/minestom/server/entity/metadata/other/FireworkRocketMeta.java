package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketMeta extends BaseEntityMeta {

    private Entity shooter;

    public FireworkRocketMeta(@NotNull Entity entity) {
        super(entity);
    }

    @NotNull
    public ItemStack getFireworkInfo() {
        return getMetadata().getIndex((byte) 7, ItemStack.getAirItem());
    }

    public void setFireworkInfo(@NotNull ItemStack value) {
        getMetadata().setIndex((byte) 7, Metadata.Slot(value));
    }

    @Nullable
    public Entity getShooter() {
        return this.shooter;
    }

    public void setShooter(@Nullable Entity value) {
        this.shooter = value;
        Integer entityID = value == null ? null : value.getEntityId();
        getMetadata().setIndex((byte) 8, Metadata.OptVarInt(entityID));
    }

    public boolean isShotAtAngle() {
        return getMetadata().getIndex((byte) 9, false);
    }

    public void setShotAtAngle(boolean value) {
        getMetadata().setIndex((byte) 9, Metadata.Boolean(value));
    }

}
