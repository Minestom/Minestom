package net.minestom.server.utils;

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

    public Position add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Position subtract(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public float getDistance(Position position) {
        return (float) Math.sqrt(MathUtils.square(position.getX() - getX()) + MathUtils.square(position.getY() - getY()) + MathUtils.square(position.getZ() - getZ()));
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

    public Position clone() {
        return new Position(getX(), getY(), getZ(), getYaw(), getPitch());
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
