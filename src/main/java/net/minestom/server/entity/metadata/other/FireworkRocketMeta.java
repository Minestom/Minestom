package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireworkRocketMeta extends EntityMeta implements ProjectileMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    private Entity shooter;

    public FireworkRocketMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public ItemStack getFireworkInfo() {
        return super.metadata.getIndex(OFFSET, ItemStack.AIR);
    }

    public void setFireworkInfo(@NotNull ItemStack value) {
        super.metadata.setIndex(OFFSET, Metadata.Slot(value));
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
        super.metadata.setIndex(OFFSET + 1, Metadata.OptVarInt(entityID));
    }

    public boolean isShotAtAngle() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setShotAtAngle(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

}
