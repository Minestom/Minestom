package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;

/**
 * The minimal view of an {@link Entity} required by the metadata tree
 * ({@link net.minestom.server.entity.metadata.EntityMeta} and its subclasses).
 *
 * <p>It exposes only lib-clean accessors so that the entire metadata tree can be
 * decoupled from the live {@link Entity} runtime. {@link Entity} implements this
 * interface, so any {@code Entity} can be passed wherever a {@code MetaTarget} is
 * expected.</p>
 */
public interface MetaTarget {

    /**
     * @return the unique entity id
     */
    int getEntityId();

    /**
     * @return the entity bounding box
     */
    BoundingBox getBoundingBox();

    /**
     * Changes the internal entity standing bounding box.
     *
     * @param width  the bounding box X size
     * @param height the bounding box Y size
     * @param depth  the bounding box Z size
     */
    void setBoundingBox(double width, double height, double depth);
}
