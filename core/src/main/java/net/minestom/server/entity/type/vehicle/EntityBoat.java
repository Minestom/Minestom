package net.minestom.server.entity.type.vehicle;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Vehicle;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.other.BoatMeta} instead.
 */
@Deprecated
public class EntityBoat extends ObjectEntity implements Vehicle {

    public EntityBoat(Position spawnPosition) {
        super(EntityType.BOAT, spawnPosition);
        setBoundingBox(1.375f, 0.5625f, 1.375f);
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    /**
     * Gets the boat type.
     *
     * @return the boat type
     */
    @NotNull
    public BoatType getBoatType() {
        final int ordinal = metadata.getIndex((byte) 10, 0);
        return BoatType.values()[ordinal];
    }

    /**
     * Changes the boat type.
     *
     * @param boatType the new boat type
     */
    public void setBoatType(@NotNull BoatType boatType) {
        this.metadata.setIndex((byte) 10, Metadata.VarInt(boatType.ordinal()));

    }

    public void refreshPaddle(boolean left, boolean right) {
        this.metadata.setIndex((byte) 11, Metadata.Boolean(left));
        this.metadata.setIndex((byte) 12, Metadata.Boolean(right));
    }

    public enum BoatType {
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK
    }


}
