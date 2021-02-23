package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntitySheep extends AgeableCreature implements Animal {

    public EntitySheep(@NotNull Position spawnPosition) {
        super(EntityType.SHEEP, spawnPosition);
        setBoundingBox(.9D, 1.3D, .9D);
    }

    public EntitySheep(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.SHEEP, spawnPosition, instance);
        setBoundingBox(.9D, 1.3D, .9D);
    }

    public int getColor() {
        return getMask() & 0x0F;
    }

    public void setColor(byte color) {
        byte before = getMask();
        byte mask = before;
        mask &= ~0x0F;
        mask |= (color & 0x0F);
        if (mask != before) {
            setMask(mask);
        }
    }

    public boolean isSheared() {
        return (getMask() & 0x10) != 0;
    }

    public void setSheared(boolean value) {
        byte mask = getMask();
        if (((mask & 0x10) != 0) == value) {
            return;
        }
        if (value) {
            mask |= 0x10;
        } else {
            mask &= ~0x10;
        }
        setMask(mask);
    }

    private byte getMask() {
        return this.metadata.getIndex((byte) 16, (byte) 0);
    }

    private void setMask(byte value) {
        this.metadata.setIndex((byte) 16, Metadata.Byte(value));
    }

}
