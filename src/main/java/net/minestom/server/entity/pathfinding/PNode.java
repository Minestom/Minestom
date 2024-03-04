package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PNode {
    public enum NodeType {
        WALK,
        JUMP,
        FALL,
        CLIMB,
        CLIMB_WALL,
        SWIM,
        FLY, REPATH
    }

    private double g;
    private double h;
    private PNode parent;
    private Point point;
    private int hashCode;

    private int cantor(int a, int b) {
        int ca = a >= 0 ? 2 * a : -2 * a - 1;
        int cb = b >= 0 ? 2 * b : -2 * b - 1;
        return (ca + cb + 1) * (ca + cb) / 2 + cb;
    }

    private NodeType type;

    public PNode(@NotNull Point point, double g, double h, @Nullable PNode parent) {
        this(point, g, h, NodeType.WALK, parent);
    }

    public PNode(@NotNull Point point, double g, double h, @NotNull NodeType type, @Nullable PNode parent) {
        this.point = new Vec(point.x(), point.y(), point.z());
        this.g = g;
        this.h = h;
        this.parent = parent;
        this.hashCode = cantor(point.blockX(), cantor(point.blockY(), point.blockZ()));
        this.type = type;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof PNode other)) return false;
        return this.hashCode == other.hashCode;
    }

    @Override
    public String toString() {
        return "PNode{" +
                "point=" + point +
                ", d=" + (g + h) +
                ", type=" + type +
                '}';
    }

    @ApiStatus.Internal
    public Point point() {
        return point;
    }

    @ApiStatus.Internal
    @NotNull NodeType getType() {
        return type;
    }

    @ApiStatus.Internal
    public double g() {
        return g;
    }

    @ApiStatus.Internal
    public double h() {
        return h;
    }

    @ApiStatus.Internal
    public void setG(double v) {
        this.g = v;
    }

    @ApiStatus.Internal
    public void setH(double heuristic) {
        this.h = heuristic;
    }

    @ApiStatus.Internal
    public void setType(@NotNull NodeType newType) {
        this.type = newType;
    }

    @ApiStatus.Internal
    public void setPoint(@NotNull Point point) {
        this.point = point;
        this.hashCode = cantor(point.blockX(), cantor(point.blockY(), point.blockZ()));
    }

    @ApiStatus.Internal
    public @Nullable PNode parent() {
        return parent;
    }

    @ApiStatus.Internal
    public void setParent(@Nullable PNode current) {
        this.parent = current;
    }
}
