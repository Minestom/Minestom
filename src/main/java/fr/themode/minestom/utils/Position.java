package fr.themode.minestom.utils;

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

    public float getDistance(Position position) {
        return (float) Math.sqrt(Math.pow(position.getX() - getX(), 2) + Math.pow(position.getY() - getY(), 2) + Math.pow(position.getZ() - getZ(), 2));
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
}
