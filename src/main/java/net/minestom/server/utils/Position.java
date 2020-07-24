package net.minestom.server.utils;

import java.util.Objects;

/**
 * Represent a position
 * The instance is not contained
 */
public class Position {

    private float x, y, z;
    private float yaw, pitch;

    public Position(float x, float y, float z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Position(float x, float y, float z) {
        this(x, y, z, 0, 0);
    }

    public Position() {
        this(0, 0, 0);
    }

    /**
     * Add offsets to the current position
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the same object position
     */
    public Position add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    /**
     * Remove offsets to the current position
     *
     * @param x the X offset
     * @param y the Y offset
     * @param z the Z offset
     * @return the same object position
     */
    public Position subtract(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    /**
     * Get the distance between 2 positions
     *
     * @param position the second position
     * @return the distance between {@code this} and {@code position}
     */
    public float getDistance(Position position) {
        return (float) Math.sqrt(MathUtils.square(position.getX() - getX()) +
                MathUtils.square(position.getY() - getY()) +
                MathUtils.square(position.getZ() - getZ()));
    }

    /**
     * Gets a unit-vector pointing in the direction that this Location is
     * facing.
     *
     * @return a vector pointing the direction of this location's {@link
     * #getPitch() pitch} and {@link #getYaw() yaw}
     */
    public Vector getDirection() {
        Vector vector = new Vector();

        float rotX = this.getYaw();
        float rotY = this.getPitch();

        vector.setY((float) -Math.sin(Math.toRadians(rotY)));

        double xz = Math.cos(Math.toRadians(rotY));

        vector.setX((float) (-xz * Math.sin(Math.toRadians(rotX))));
        vector.setZ((float) (xz * Math.cos(Math.toRadians(rotX))));

        return vector;
    }

    /**
     * Sets the {@link #getYaw() yaw} and {@link #getPitch() pitch} to point
     * in the direction of the vector.
     */
    public Position setDirection(Vector vector) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double _2PI = 2 * Math.PI;
        final float x = vector.getX();
        final float z = vector.getZ();

        if (x == 0 && z == 0) {
            pitch = vector.getY() > 0 ? -90 : 90;
            return this;
        }

        double theta = Math.atan2(-x, z);
        yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);

        float x2 = MathUtils.square(x);
        float z2 = MathUtils.square(z);
        float xz = (float) Math.sqrt(x2 + z2);
        pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));

        return this;
    }

    /**
     * Set the x/y/z field of this position to the value of {@code position}
     *
     * @param position the vector to copy the values from
     */
    public void copy(Vector position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
    }

    /**
     * Set the x/y/z field of this position to the value of {@code position}
     *
     * @param position the position to copy the values from
     */
    public void copy(Position position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        this.yaw = position.getYaw();
        this.pitch = position.getPitch();
    }

    /**
     * Clone this position object with the same values
     *
     * @return a new {@link Position} object with the same coordinates
     */
    public Position clone() {
        return new Position(getX(), getY(), getZ(), getYaw(), getPitch());
    }

    /**
     * Get if the two objects are position and have the same values
     *
     * @param o the position to check the equality
     * @return true if the two objects are position with the same values, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Float.compare(position.x, x) == 0 &&
                Float.compare(position.y, y) == 0 &&
                Float.compare(position.z, z) == 0 &&
                Float.compare(position.yaw, yaw) == 0 &&
                Float.compare(position.pitch, pitch) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public BlockPosition toBlockPosition() {
        return new BlockPosition(x, y, z);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "Position[" + x + ":" + y + ":" + z + "] (" + yaw + "/" + pitch + ")";
    }
}
