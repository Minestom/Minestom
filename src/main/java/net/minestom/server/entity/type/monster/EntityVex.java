package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityVex extends EntityCreature implements Monster {

    private final static byte ATTACKING_BIT = 0x1;

    public EntityVex(@NotNull Position spawnPosition) {
        super(EntityType.VEX, spawnPosition);
        setBoundingBox(.4D, .8D, .4D);
    }

    public EntityVex(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.VEX, spawnPosition, instance);
        setBoundingBox(.4D, .8D, .4D);
    }

    public boolean isAttacking() {
        return (getMask() & ATTACKING_BIT) != 0;
    }

    public void setAttacking(boolean value) {
        modifyMask(ATTACKING_BIT, value);
    }

    private byte getMask() {
        return this.metadata.getIndex((byte) 15, (byte) 0);
    }

    private void setMask(byte mask) {
        this.metadata.setIndex((byte) 15, Metadata.Byte(mask));
    }

    private void modifyMask(byte bit, boolean value) {
        byte mask = getMask();
        boolean isPresent = (mask & bit) == bit;
        if (isPresent == value) {
            return;
        }
        if (value) {
            mask |= bit;
        } else {
            mask &= ~bit;
        }
        setMask(mask);
    }

}
