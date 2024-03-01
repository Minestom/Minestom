package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public class PNode {
    public double g() {
        return g;
    }

    public void setG(double v) {
        this.g = v;
    }

    public void setH(double heuristic) {
        this.h = heuristic;
    }

    public enum NodeType {
        WALK,
        JUMP,
        FALL,
        CLIMB,
        CLIMB_WALL,
        SWIM,
        FLY, REPATH
    }

    double g;
    double h;
    PNode parent;
    Point point;
    int hashCode;

    int cantor(int a, int b) {
        return (a + b + 1) * (a + b) / 2 + b;
    }

    private NodeType type;

    public void setType(NodeType newType) {
        this.type = newType;
    }

    public NodeType getType() {
        return type;
    }

    public void setPoint(Point point) {
        this.point = point;
        this.hashCode = cantor(point.blockX(), cantor(point.blockY(), point.blockZ()));
    }

    public PNode(Point point, double g, double h, PNode parent) {
        this(point, g, h, NodeType.WALK, parent);
    }

    public PNode(Point point, double g, double h, NodeType type, PNode parent) {
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

    public Point point() {
        return point;
    }
}
