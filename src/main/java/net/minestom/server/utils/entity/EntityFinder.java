package net.minestom.server.utils.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.math.IntRange;

import java.util.ArrayList;

/**
 * Represent a query which can be call to find one or multiple entities
 * It is based on the target selectors used in commands
 */
public class EntityFinder {

    // Commands option
    private boolean onlySingleEntity;
    private boolean onlyPlayers;

    // Simple float
    private float x, y, z;
    private float dx, dy, dz;

    // Range
    private IntRange distance;
    private IntRange level;

    // By traits
    private int limit;
    private EntitySort entitySort;
    private EntityType entityType;

    public boolean isOnlySingleEntity() {
        return onlySingleEntity;
    }

    public void setOnlySingleEntity(boolean onlySingleEntity) {
        this.onlySingleEntity = onlySingleEntity;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers;
    }

    public void setOnlyPlayers(boolean onlyPlayers) {
        this.onlyPlayers = onlyPlayers;
    }

    /**
     * Find a list of entities (could be empty) based on the conditions
     *
     * @return all entities validating the conditions
     */
    public ArrayList<Entity> find() {
        return new ArrayList<>();
    }

    public enum EntitySort {
        ARBITRARY, FURTHEST, NEAREST, RANDOM
    }
}
