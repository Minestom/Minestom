package net.minestom.server.collision;

public final class SweepResult {
    public static SweepResult NO_COLLISION  = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0);

    double res;
    double normalX, normalY, normalZ;
    double collidedPositionX, collidedPositionY, collidedPositionZ;
    Shape collidedShape;

    /**
     * Store the result of a movement operation
     *
     * @param res     Percentage of move completed
     * @param normalX -1 if intersected on left, 1 if intersected on right
     * @param normalY -1 if intersected on bottom, 1 if intersected on top
     * @param normalZ -1 if intersected on front, 1 if intersected on back
     */
    public SweepResult(double res, double normalX, double normalY, double normalZ, Shape collidedShape, double collidedPosX, double collidedPosY, double collidedPosZ) {
        this.res = res;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.collidedShape = collidedShape;
        this.collidedPositionX = collidedPosX;
        this.collidedPositionY = collidedPosY;
        this.collidedPositionZ = collidedPosZ;
    }
}
