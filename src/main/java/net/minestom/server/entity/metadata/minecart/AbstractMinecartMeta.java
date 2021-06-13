package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMinecartMeta extends EntityMeta implements ObjectDataProvider {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 6;

    protected AbstractMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getShakingPower() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setShakingPower(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    public int getShakingDirection() {
        return super.metadata.getIndex(OFFSET + 1, 1);
    }

    public void setShakingDirection(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public float getShakingMultiplier() {
        return super.metadata.getIndex(OFFSET + 2, 0F);
    }

    public void setShakingMultiplier(float value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Float(value));
    }

    public int getCustomBlockIdAndDamage() {
        return super.metadata.getIndex(OFFSET + 3, 0);
    }

    public void setCustomBlockIdAndDamage(int value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.VarInt(value));
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return super.metadata.getIndex(OFFSET + 4, 6);
    }

    public void setCustomBlockYPosition(int value) {
        super.metadata.setIndex(OFFSET + 4, Metadata.VarInt(value));
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
