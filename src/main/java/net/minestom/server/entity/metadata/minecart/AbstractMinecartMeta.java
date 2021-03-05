package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMinecartMeta extends EntityMeta implements ObjectDataProvider {

    protected AbstractMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getShakingPower() {
        return super.metadata.getIndex((byte) 7, 0);
    }

    public void setShakingPower(int value) {
        super.metadata.setIndex((byte) 7, Metadata.VarInt(value));
    }

    public int getShakingDirection() {
        return super.metadata.getIndex((byte) 8, 1);
    }

    public void setShakingDirection(int value) {
        super.metadata.setIndex((byte) 8, Metadata.VarInt(value));
    }

    public float getShakingMultiplier() {
        return super.metadata.getIndex((byte) 9, 0F);
    }

    public void setShakingMultiplier(float value) {
        super.metadata.setIndex((byte) 9, Metadata.Float(value));
    }

    public int getCustomBlockIdAndDamage() {
        return super.metadata.getIndex((byte) 10, 0);
    }

    public void setCustomBlockIdAndDamage(int value) {
        super.metadata.setIndex((byte) 10, Metadata.VarInt(value));
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return super.metadata.getIndex((byte) 11, 6);
    }

    public void setCustomBlockYPosition(int value) {
        super.metadata.setIndex((byte) 11, Metadata.VarInt(value));
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
