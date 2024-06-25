package net.minestom.server.entity.metadata;

public interface AgeableMeta {
    boolean isBaby();

    /**
     * Forcefully sets the bounding box of this entity to the bounding box
     * which the client expects.
     * @param value true if setting this entity to a baby
     */
    void setBaby(boolean value);
}
