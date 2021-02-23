package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.entity.type.Animal;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityAbstractHorse extends AgeableCreature implements Animal {

    private final static byte TAMED_BIT = 0x02;
    private final static byte SADDLED_BIT = 0x04;
    private final static byte HAS_BRED_BIT = 0x08;
    private final static byte EATING_BIT = 0x10;
    private final static byte REARING_BIT = 0x20;
    private final static byte MOUTH_OPEN_BIT = 0x40;

    EntityAbstractHorse(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    EntityAbstractHorse(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean isTamed() {
        return (getMask() & TAMED_BIT) != 0;
    }

    public void setTamed(boolean value) {
        modifyMask(TAMED_BIT, value);
    }

    public boolean isSaddled() {
        return (getMask() & SADDLED_BIT) != 0;
    }

    public void setSaddled(boolean value) {
        modifyMask(SADDLED_BIT, value);
    }

    public boolean isHasBred() {
        return (getMask() & HAS_BRED_BIT) != 0;
    }

    public void setHasBred(boolean value) {
        modifyMask(HAS_BRED_BIT, value);
    }

    public boolean isEating() {
        return (getMask() & EATING_BIT) != 0;
    }

    public void setEating(boolean value) {
        modifyMask(EATING_BIT, value);
    }

    public boolean isRearing() {
        return (getMask() & REARING_BIT) != 0;
    }

    public void setRearing(boolean value) {
        modifyMask(REARING_BIT, value);
    }

    public boolean isMouthOpen() {
        return (getMask() & MOUTH_OPEN_BIT) != 0;
    }

    public void setMouthOpen(boolean value) {
        modifyMask(MOUTH_OPEN_BIT, value);
    }

    public UUID getOwner() {
        return this.metadata.getIndex((byte) 17, null);
    }

    public void setOwner(UUID value) {
        this.metadata.setIndex((byte) 17, Metadata.OptUUID(value));
    }

    private byte getMask() {
        return this.metadata.getIndex((byte) 16, (byte) 0);
    }

    private void setMask(byte mask) {
        this.metadata.setIndex((byte) 16, Metadata.Byte(mask));
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
