package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;

public record Quaternion(float w, float x, float y, float z) {
    public static final Quaternion ZERO = new Quaternion(0, 0, 0, 0);

    /**
     * Returns the conjugate quaternion of this quaternion.
     *
     * @return the conjugate quaternion
     */
    public @NotNull Quaternion getConjugate() {
        return new Quaternion(w, -x, -y, -z);
    }

    public @NotNull Quaternion add(@NotNull Quaternion other) {
        return add(other.w, other.x, other.y, other.z);
    }

    public @NotNull Quaternion add(float w, float x, float y, float z) {
        return new Quaternion(
                this.w + w,
                this.x + x,
                this.y + y,
                this.z + z
        );
    }

    public @NotNull Quaternion sub(@NotNull Quaternion other) {
        return sub(other.w, other.z, other.y, other.z);
    }

    public @NotNull Quaternion sub(float w, float x, float y, float z) {
        return new Quaternion(
                this.w - w,
                this.x - x,
                this.y - y,
                this.z - z
        );
    }

    public @NotNull Quaternion mul(@NotNull Quaternion other) {
        return mul(other.w, other.x, other.y, other.z);
    }

    public @NotNull Quaternion mul(float w, float x, float y, float z) {
        float newW = this.w * w - this.x * x - this.y * y - this.z * z;
        float newX = this.w * x + this.x * w + this.y * z - this.z * y;
        float newY = this.w * y - this.x * z + this.y * w + this.z * x;
        float newZ = this.w * z + this.x * y - this.y * x + this.z * w;
        return new Quaternion(newW, newX, newY, newZ);
    }

    public @NotNull Quaternion mul(float alpha) {
        return new Quaternion(
                this.w * alpha,
                this.x * alpha,
                this.y * alpha,
                this.z * alpha
        );
    }

    /**
     * Computes the norm of this quaternion.
     *
     * @return the norm
     */
    public double getNorm() {
        return Math.sqrt(w * w +
                x * x +
                y * y +
                z * z);
    }

    /**
     * Computes the normalized quaternion (the versor of this quaternion).
     * The norm of this quaternion must not be zero.
     *
     * @return a normalized quaternion
     */
    public @NotNull Quaternion normalize() {
        final double norm = getNorm();
        return new Quaternion(
                (float) (w / norm),
                (float) (x / norm),
                (float) (y / norm),
                (float) (z / norm)
        );
    }

    public double dot(@NotNull Quaternion other) {
        return dot(other.w, other.x, other.y, other.z);
    }

    public double dot(float w, float x, float y, float z) {
        return this.w * w +
                this.x * x +
                this.y * y +
                this.z * z;
    }
}
