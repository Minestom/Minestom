package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractMinecartMeta extends EntityMeta {

    protected AbstractMinecartMeta(@NotNull Entity entity) {
        super(entity);
    }

    public int getShakingPower() {
        return getMetadata().getIndex((byte) 7, 0);
    }

    public void setShakingPower(int value) {
        getMetadata().setIndex((byte) 7, Metadata.VarInt(value));
    }

    public int getShakingDirection() {
        return getMetadata().getIndex((byte) 8, 1);
    }

    public void setShakingDirection(int value) {
        getMetadata().setIndex((byte) 8, Metadata.VarInt(value));
    }

    public float getShakingMultiplier() {
        return getMetadata().getIndex((byte) 9, 0F);
    }

    public void setShakingMultiplier(float value) {
        getMetadata().setIndex((byte) 9, Metadata.Float(value));
    }

    public int getCustomBlockIdAndDamage() {
        return getMetadata().getIndex((byte) 10, 0);
    }

    public void setCustomBlockIdAndDamage(int value) {
        getMetadata().setIndex((byte) 10, Metadata.VarInt(value));
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return getMetadata().getIndex((byte) 11, 6);
    }

    public void setCustomBlockYPosition(int value) {
        getMetadata().setIndex((byte) 11, Metadata.VarInt(value));
    }

}
