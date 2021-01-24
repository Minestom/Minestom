package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a position.
 * The instance is not contained.
 */
public class Position implements PublicCloneable<Position> {

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
     * Adds offsets to the current position.
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
     * Adds a position to the current position.
     *
     * @param position the position to add to this
     * @return the same object position
     */
    public Position add(Position position) {
        this.x += position.x;
        this.y += position.y;
        this.z += position.z;
        return this;
    }

    /**
     * Removes offsets to the current position.
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

    public float getDistance(float x, float y, float z) {
        return (float) Math.sqrt(MathUtils.square(x - getX()) +
                MathUtils.square(y - getY()) +
                MathUtils.square(z - getZ()));
    }

    /**
     * Gets the distance between 2 positions.
     * In cases where performance matters, {@link #getDistanceSquared(Position)} should be used
     * as it does not perform the expensive Math.sqrt method.
     *
     * @param position the second position
     * @return the distance between {@code this} and {@code position}
     */
    public float getDistance(@NotNull Position position) {
        return getDistance(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Gets the square distance to another position.
     *
     * @param position the second position
     * @return the squared distance between {@code this} and {@code position}
     */
    public float getDistanceSquared(@NotNull Position position) {
        return MathUtils.square(getX() - position.getX()) +
                MathUtils.square(getY() - position.getY()) +
                MathUtils.square(getZ() - position.getZ());
    }

    /**
     * Gets a unit-vector pointing in the direction that this Location is
     * facing.
     *
     * @return a vector pointing the direction of this location's {@link
     * #getPitch() pitch} and {@link #getYaw() yaw}
     */
    @NotNull
    public Vector getDirection() {
        Vector vector = new Vector();

        final float rotX = this.getYaw();
        final float rotY = this.getPitch();

        vector.setY((float) -Math.sin(Math.toRadians(rotY)));

        final double xz = Math.cos(Math.toRadians(rotY));

        vector.setX((float) (-xz * Math.sin(Math.toRadians(rotX))));
        vector.setZ((float) (xz * Math.cos(Math.toRadians(rotX))));

        return vector;
    }

    /**
     * Sets the {@link #getYaw() yaw} and {@link #getPitch() pitch} to point
     * in the direction of the vector.
     */
    @NotNull
    public Position setDirection(@NotNull Vector vector) {
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

        final double theta = Math.atan2(-x, z);
        yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);

        final float x2 = MathUtils.square(x);
        final float z2 = MathUtils.square(z);
        final float xz = (float) Math.sqrt(x2 + z2);
        pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));

        return this;
    }

    /**
     * Sets the x/y/z field of this position to the value of {@code position}.
     *
     * @param position the vector to copy the values from
     */
    public void copy(@NotNull Vector position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
    }

    /**
     * Sets the x/y/z/yaw/pitch fields of this position to the value of {@code position}.
     *
     * @param position the position to copy the values from
     */
    public void copy(@NotNull Position position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        this.yaw = position.getYaw();
        this.pitch = position.getPitch();
    }

    /**
     * Sets the x/y/z fields of this position to the value of {@code position}.
     *
     * @param position the position to copy the values from
     */
    public void copyCoordinates(@NotNull Position position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
    }

    @NotNull
    @Override
    public Position clone() {
        try {
            return (Position) super.clone();
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            return null;
        }
    }

    /**
     * @deprecated Please use {@link #clone()}
     */
    @Deprecated
    public Position copy() {
        return clone();
    }

    /**
     * Gets if the two objects are position and have the same values.
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

    /**
     * Checks it two positions are similar (x/y/z).
     *
     * @param position the position to compare
     * @return true if the two positions are similar
     */
    public boolean isSimilar(@NotNull Position position) {
        return Float.compare(position.x, x) == 0 &&
                Float.compare(position.y, y) == 0 &&
                Float.compare(position.z, z) == 0;
    }

    /**
     * Checks if two positions have a similar view (yaw/pitch).
     *
     * @param position the position to compare
     * @return true if the two positions have the same view
     */
    public boolean hasSimilarView(@NotNull Position position) {
        return Float.compare(position.yaw, yaw) == 0 &&
                Float.compare(position.pitch, pitch) == 0;
    }

    /**
     * Gets if two positions are in the same chunk.
     *
     * @param position the checked position chunk
     * @return true if 'this' is in the same chunk as {@code position}
     */
    public boolean inSameChunk(@NotNull Position position) {
        final int chunkX1 = ChunkUtils.getChunkCoordinate((int) getX());
        final int chunkZ1 = ChunkUtils.getChunkCoordinate((int) getZ());

        final int chunkX2 = ChunkUtils.getChunkCoordinate((int) position.getX());
        final int chunkZ2 = ChunkUtils.getChunkCoordinate((int) position.getZ());

        return chunkX1 == chunkX2 && chunkZ1 == chunkZ2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    /**
     * Gets the position X.
     *
     * @return the position X
     */
    public float getX() {
        return x;
    }

    /**
     * Changes the position X.
     *
     * @param x the new position X
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Gets the position Y.
     *
     * @return the position Y
     */
    public float getY() {
        return y;
    }

    /**
     * Changes the position Y.
     *
     * @param y the new position Y
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Gets the position Z.
     *
     * @return the position Z
     */
    public float getZ() {
        return z;
    }

    /**
     * Changes the position Z.
     *
     * @param z the new position Z
     */
    public void setZ(float z) {
        this.z = z;
    }

    /**
     * Gets the position yaw.
     *
     * @return the yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Changes the position yaw.
     *
     * @param yaw the new yaw
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * Gets the position pitch.
     *
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Changes the position pitch.
     *
     * @param pitch the new pitch
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Converts this position to a {@link BlockPosition}.
     *
     * @return the converted {@link BlockPosition}
     */
    public BlockPosition toBlockPosition() {
        return new BlockPosition(x, y, z);
    }

    /**
     * Converts this position to a {@link Vector}.
     *
     * @return the converted {@link Vector}
     */
    public Vector toVector() {
        return new Vector(x, y, z);
    }

    @Override
    public String toString() {
        return "Position[" + x + ":" + y + ":" + z + "] (" + yaw + "/" + pitch + ")";
    }
}
