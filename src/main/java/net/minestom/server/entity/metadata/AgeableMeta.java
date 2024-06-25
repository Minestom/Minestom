package net.minestom.server.entity.metadata;

public interface AgeableMeta {
    boolean isBaby();

    /**
     * Sets the {@link AgeableMeta#isBaby()} metadata of this entity.
     * <p>
     * Note: this will forcefully set the bounding box of this entity to the bounding box
     * that the client expects.
     * @param value true if setting to a baby, false otherwise
     */
    void setBaby(boolean value);
}
