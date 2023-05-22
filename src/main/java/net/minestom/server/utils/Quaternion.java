package net.minestom.server.utils;

public record Quaternion(float x, float y, float z, float w) {
    public Quaternion mul(Quaternion other) {
        float newX = w * other.x + x * other.w + y * other.z - z * other.y;
        float newY = w * other.y - x * other.z + y * other.w + z * other.x;
        float newZ = w * other.z + x * other.y - y * other.x + z * other.w;
        float newW = w * other.w - x * other.x - y * other.y - z * other.z;
        return new Quaternion(newX, newY, newZ, newW);
    }

    // TODO add more math operations
}
