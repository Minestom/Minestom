package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.Locale;
import java.util.Set;

public final class PathTypeResolver {
    private static final Set<String> FIRE_LIKE = Set.of("fire", "soul_fire", "magma_block", "lava_cauldron");
    private static final Set<String> TRAPDOORS = Set.of("trapdoor", "big_dripleaf", "lily_pad");
    private static final Set<String> CARPET_SUFFIX = Set.of("_carpet", "moss_carpet", "pale_moss_carpet");

    private PathTypeResolver() {}

    public static PathType getPathType(Block.Getter getter, Point point) {
        Block block = getter.getBlock(point.blockX(), point.blockY(), point.blockZ(), Block.Getter.Condition.NONE);
        PathType typeHere = classify(block);

        if (isWalkableSpace(typeHere)) {
            Block below = getter.getBlock(point.blockX(), point.blockY() - 1, point.blockZ(), Block.Getter.Condition.NONE);
            PathType belowType = classify(below);
            if (belowType == PathType.WATER) return PathType.WATER_BORDER;
            if (belowType == PathType.LAVA || belowType == PathType.DAMAGE_FIRE) return PathType.DANGER_FIRE;
            if (belowType == PathType.POWDER_SNOW) return PathType.DANGER_POWDER_SNOW;
        }

        return typeHere;
    }

    private static boolean isWalkableSpace(PathType type) {
        return type == PathType.OPEN || type == PathType.WALKABLE || type == PathType.WALKABLE_DOOR || type == PathType.TRAPDOOR;
    }

    private static PathType classify(Block block) {
        if (block.isAir()) return PathType.OPEN;
        if (block.isLiquid()) {
            if (block.compare(Block.WATER)) return PathType.WATER;
            if (block.compare(Block.LAVA)) return PathType.LAVA;
        }

        final String key = block.key().value().toLowerCase(Locale.ROOT);

        if (key.contains("powder_snow")) return PathType.POWDER_SNOW;
        if (key.contains("rail")) return PathType.RAIL;
        if (key.contains("honey_block")) return PathType.STICKY_HONEY;
        if (key.contains("cocoa")) return PathType.COCOA;

        for (String suffix : CARPET_SUFFIX) {
            if (key.endsWith(suffix)) return PathType.WALKABLE;
        }

        if (key.contains("door")) {
            final boolean open = "true".equals(block.getProperty("open"));
            if (open) return PathType.DOOR_OPEN;
            if (key.contains("iron")) return PathType.DOOR_IRON_CLOSED;
            return PathType.DOOR_WOOD_CLOSED;
        }

        if (key.contains("fence_gate")) {
            final boolean open = "true".equals(block.getProperty("open"));
            return open ? PathType.OPEN : PathType.FENCE;
        }

        if (key.contains("fence") || key.contains("wall")) return PathType.FENCE;

        if (TRAPDOORS.stream().anyMatch(key::contains)) {
            final boolean open = "true".equals(block.getProperty("open"));
            return open ? PathType.WALKABLE_DOOR : PathType.TRAPDOOR;
        }

        if (key.contains("leaves")) return PathType.LEAVES;
        if (key.contains("ladder") || key.contains("vine")) return PathType.WALKABLE;

        if (key.contains("sweet_berry_bush") || key.contains("cactus") || key.contains("wither_rose") || key.contains("pointed_dripstone")) {
            return PathType.DAMAGE_OTHER;
        }

        if (FIRE_LIKE.contains(key)) return PathType.DAMAGE_FIRE;
        if (key.contains("campfire")) {
            final boolean lit = !"false".equals(block.getProperty("lit"));
            return lit ? PathType.DAMAGE_FIRE : PathType.OPEN;
        }

        if (!block.isSolid()) return PathType.OPEN;

        return PathType.WALKABLE;
    }
}
