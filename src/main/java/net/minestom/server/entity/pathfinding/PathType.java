package net.minestom.server.entity.pathfinding;

public enum PathType {
    BLOCKED(-1.0F),
    OPEN(0.0F),
    WALKABLE(0.0F),
    WALKABLE_DOOR(0.0F),
    TRAPDOOR(0.0F),
    POWDER_SNOW(-1.0F),
    DANGER_POWDER_SNOW(0.0F),
    FENCE(-1.0F),
    LAVA(-1.0F),
    WATER(8.0F),
    WATER_BORDER(8.0F),
    RAIL(0.0F),
    UNPASSABLE_RAIL(-1.0F),
    DANGER_FIRE(8.0F),
    DAMAGE_FIRE(16.0F),
    DANGER_OTHER(8.0F),
    DAMAGE_OTHER(-1.0F),
    DOOR_OPEN(0.0F),
    DOOR_WOOD_CLOSED(-1.0F),
    DOOR_IRON_CLOSED(-1.0F),
    BREACH(4.0F),
    LEAVES(-1.0F),
    STICKY_HONEY(8.0F),
    COCOA(0.0F),
    DAMAGE_CAUTIOUS(0.0F),
    DANGER_TRAPDOOR(0.0F);

    private final float malus;

    PathType(float malus) {
        this.malus = malus;
    }

    public float getMalus() {
        return malus;
    }
}
