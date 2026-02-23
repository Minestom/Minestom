package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class PNode {
    public enum Type {
        WALK, JUMP, FALL, CLIMB, CLIMB_WALL, SWIM, FLY, REPATH
    }
    private double g;
    private double h;
    private PNode parent;
    private double pointX;
    private double pointY;
    private double pointZ;
    private int hashCode;
    private PathType pathType = PathType.WALKABLE;
    private float costMalus = 0;
    private boolean closed = false;
    private Type type;
    public PNode(double px, double py, double pz, double g, double h, @Nullable PNode parent) {
        this(px, py, pz, g, h, Type.WALK, parent);
    }

    public PNode(double px, double py, double pz, double g, double h, PNode.Type type, @Nullable PNode parent) {
        this.g = g;
        this.h = h;
        this.parent = parent;
        this.type = type;
        this.pointX = px;
        this.pointY = py;
        this.pointZ = pz;
        this.hashCode = cantor((int) Math.floor(px), cantor((int) Math.floor(py), (int) Math.floor(pz)));
    }

    public PNode(Point point, double g, double h, Type walk, @Nullable PNode parent) {
        this(point.x(), point.y(), point.z(), g, h, walk, parent);
    }

    private static int cantor(int a, int b) {
        int ca = a >= 0 ? 2 * a : -2 * a - 1;
        int cb = b >= 0 ? 2 * b : -2 * b - 1;
        return (ca + cb + 1) * (ca + cb) / 2 + cb;
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
        return "PNode{" + "point=" + pointX + ", " + pointY + ", " + pointZ + ", d=" + (g + h) + ", type=" + type + '}';
    }

    @ApiStatus.Internal
    public double x() {
        return pointX;
    }

    @ApiStatus.Internal
    public double y() {
        return pointY;
    }

    @ApiStatus.Internal
    public double z() {
        return pointZ;
    }

    public int blockX() {
        return (int) Math.floor(pointX);
    }

    public int blockY() {
        return (int) Math.floor(pointY);
    }

    public int blockZ() {
        return (int) Math.floor(pointZ);
    }

    @ApiStatus.Internal
    public Type getType() {
        return type;
    }

    @ApiStatus.Internal
    public void setType(PNode.Type newType) {
        this.type = newType;
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
    public void setPoint(double px, double py, double pz) {
        this.pointX = px;
        this.pointY = py;
        this.pointZ = pz;
        this.hashCode = cantor((int) Math.floor(px), cantor((int) Math.floor(py), (int) Math.floor(pz)));
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public float getCostMalus() {
        return costMalus;
    }

    public void setCostMalus(float costMalus) {
        this.costMalus = costMalus;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Point point() {
        return new Vec(pointX, pointY, pointZ);
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
