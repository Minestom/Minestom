package net.minestom.server.utils;

public class BlockPosition {

    private int x, y, z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(float x, float y, float z) {
        final int castedY = (int) y;

        this.x = (int) (x < 0 ? x - 1 : x);
        this.y = (y == castedY) ? castedY : castedY + 1;
        this.z = (int) (z < 0 ? z - 1 : z);
    }

    public BlockPosition add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public BlockPosition subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getDistance(BlockPosition blockPosition) {
        return Math.abs(getX() - blockPosition.getX()) +
                Math.abs(getY() - blockPosition.getY()) +
                Math.abs(getZ() - blockPosition.getZ());
    }

    public BlockPosition clone() {
        return new BlockPosition(x, y, z);
    }

    public Position toPosition() {
        return new Position(x, y, z);
    }

    @Override
    public String toString() {
        return "BlockPosition[" + x + ":" + y + ":" + z + "]";
    }
}
