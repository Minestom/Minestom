package net.minestom.server.entity.type;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TameableAnimalCreature extends AgeableCreature implements TameableAnimal {

    private final static byte SITTING_BIT = 0x1;
    private final static byte TAMED_BIT = 0x4;

    protected TameableAnimalCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
    }

    protected TameableAnimalCreature(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
    }

    public boolean isSitting() {
        return (getMask() & SITTING_BIT) != 0;
    }

    public void setSitting(boolean value) {
        modifyMask(SITTING_BIT, value);
    }

    public boolean isTamed() {
        return (getMask() & TAMED_BIT) != 0;
    }

    public void setTamed(boolean value) {
        modifyMask(TAMED_BIT, value);
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
