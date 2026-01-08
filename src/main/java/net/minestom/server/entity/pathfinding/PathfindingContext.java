package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public final class PathfindingContext {
    private final Block.Getter getter;
    private final BlockVec mobPosition;

    public PathfindingContext(Block.Getter getter, Point mobPosition) {
        this.getter = getter;
        this.mobPosition = new BlockVec(mobPosition);
    }

    public PathType getPathTypeFromState(int x, int y, int z) {
        return PathTypeResolver.getPathType(getter, new BlockVec(x, y, z));
    }

    public Block getBlock(int x, int y, int z) {
        return getter.getBlock(x, y, z, Block.Getter.Condition.NONE);
    }

    public Block.Getter level() {
        return getter;
    }

    public BlockVec mobPosition() {
        return mobPosition;
    }

    public @Nullable Block getBlock(Point point) {
        return getter.getBlock(point, Block.Getter.Condition.NONE);
    }
}
