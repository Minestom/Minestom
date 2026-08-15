package net.minestom.server.instance;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.network.packet.server.play.InitializeWorldBorderPacket;
import net.minestom.server.network.packet.server.play.WorldBorderCenterPacket;
import net.minestom.server.network.packet.server.play.WorldBorderLerpSizePacket;
import net.minestom.server.network.packet.server.play.WorldBorderSizePacket;
import net.minestom.server.network.packet.server.play.WorldBorderWarningDelayPacket;
import net.minestom.server.network.packet.server.play.WorldBorderWarningReachPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;

/**
 * Represents the world border state of an {@link Instance},
 * can be retrieved with {@link Instance#getWorldBorder()}.
 *
 * @param diameter                  the diameter of this world border
 * @param centerX                   the center x coordinate of this world border
 * @param centerZ                   the center z coordinate of this world border
 * @param warningDistance           the distance from this world border before
 *                                  the warning indicator is displayed
 * @param warningTime               the length of time the warning indicator
 *                                  is displayed
 * @param dimensionTeleportBoundary restricts the distance travelled when entering
 *                                  this world from another dimension (should be at
 *                                  least the diameter of the world border)
 */
public record WorldBorder(double diameter, double centerX, double centerZ, int warningDistance, int warningTime,
                          int dimensionTeleportBoundary) {
    public static final WorldBorder DEFAULT_BORDER = new WorldBorder(ServerFlag.WORLD_BORDER_SIZE * 2, 0, 0, 5, 15, ServerFlag.WORLD_BORDER_SIZE);

    /**
     * @throws IllegalArgumentException if {@code diameter} is less than 0 or NaN, or if a center coordinate is NaN
     */
    public WorldBorder {
        Check.argCondition(!(diameter >= 0), "Diameter should be >= 0");
        Check.argCondition(Double.isNaN(centerX) || Double.isNaN(centerZ), "Center cannot be NaN");
    }

    public WorldBorder(double diameter, double centerX, double centerZ, int warningDistance, int warningTime) {
        this(diameter, centerX, centerZ, warningDistance, warningTime, ServerFlag.WORLD_BORDER_SIZE);
    }

    @Contract(pure = true)
    public WorldBorder withDiameter(double diameter) {
        return new WorldBorder(diameter, centerX, centerZ, warningDistance, warningTime, dimensionTeleportBoundary);
    }

    @Contract(pure = true)
    public WorldBorder withCenter(double centerX, double centerZ) {
        return new WorldBorder(diameter, centerX, centerZ, warningDistance, warningTime, dimensionTeleportBoundary);
    }

    @Contract(pure = true)
    public WorldBorder withWarningDistance(int warningDistance) {
        return new WorldBorder(diameter, centerX, centerZ, warningDistance, warningTime, dimensionTeleportBoundary);
    }

    @Contract(pure = true)
    public WorldBorder withWarningTime(int warningTime) {
        return new WorldBorder(diameter, centerX, centerZ, warningDistance, warningTime, dimensionTeleportBoundary);
    }

    /**
     * Used to know if a position is located inside the world border or not.
     * <p>
     * The maximum bound is exclusive. A point exactly on the maximum
     * x or z bound is outside the world border.
     *
     * @param point the point to check
     * @return true if {@code point} is inside the world border, false otherwise
     */
    public boolean inBounds(Point point) {
        final double radius = diameter / 2;
        return point.x() >= centerX - radius && point.x() < centerX + radius &&
                point.z() >= centerZ - radius && point.z() < centerZ + radius;
    }

    /**
     * Checks whether a bounding box at the given position is entirely inside this world border.
     * <p>
     * The check uses the exact border bounds, with inclusive maximums. Collisions use walls
     * expanded outward to whole block coordinates, so when the diameter is not a whole number
     * an entity stopped by the border may rest slightly outside these bounds.
     *
     * @param position the position of the bounding box
     * @param boundingBox the relative bounding box
     * @return true if the complete bounding box is inside the world border
     */
    public boolean inBounds(Point position, BoundingBox boundingBox) {
        final double radius = diameter / 2;
        final double minX = centerX - radius;
        final double maxX = centerX + radius;
        final double minZ = centerZ - radius;
        final double maxZ = centerZ + radius;
        return position.x() + boundingBox.minX() >= minX && position.x() + boundingBox.maxX() <= maxX &&
                position.z() + boundingBox.minZ() >= minZ && position.z() + boundingBox.maxZ() <= maxZ;
    }

    /**
     * Used to know if an entity is located inside the world border or not.
     *
     * @param entity the entity to check
     * @return true if {@code entity} is inside the world border, false otherwise
     * @deprecated Use {@link #inBounds(Point, BoundingBox)} instead. This method now checks
     * the complete bounding box where it previously checked only the entity position.
     */
    @Deprecated(forRemoval = true)
    public boolean inBounds(Entity entity) {
        return inBounds(entity.getPosition(), entity.getBoundingBox());
    }

    /**
     * Creates a {@link InitializeWorldBorderPacket} which dictates every property
     * of the world border.
     *
     * @param targetDiameter the target diameter if there is a current lerp in progress
     * @param transitionTime the transition time in milliseconds of the current
     *                       lerp in progress
     * @return an {@link InitializeWorldBorderPacket} reflecting the
     * properties of this border
     */
    public InitializeWorldBorderPacket createInitializePacket(double targetDiameter, long transitionTime) {
        return new InitializeWorldBorderPacket(centerX, centerZ, diameter, targetDiameter, transitionTime, dimensionTeleportBoundary, warningTime, warningDistance);
    }

    /**
     * Creates a {@link WorldBorderSizePacket} which dictates the origin of the world border.
     *
     * @return the {@link WorldBorderSizePacket} with the center values of this world border
     */
    public WorldBorderCenterPacket createCenterPacket() {
        return new WorldBorderCenterPacket(centerX, centerZ);
    }

    /**
     * Creates a {@link WorldBorderLerpSizePacket} which lerps the border from its current
     * diameter to the target diameter over the given transition time.
     *
     * @param targetDiameter the final diameter of the border after this transition
     * @param transitionTime the transition time in milliseconds for this lerp
     * @return the {@link WorldBorderLerpSizePacket} representing this lerp
     */
    public WorldBorderLerpSizePacket createLerpSizePacket(double targetDiameter, long transitionTime) {
        return new WorldBorderLerpSizePacket(diameter, targetDiameter, transitionTime);
    }

    /**
     * Creates a {@link WorldBorderSizePacket} with this world border's diameter.
     *
     * @return the {@link WorldBorderSizePacket} with this world border's diameter
     */
    public WorldBorderSizePacket createSizePacket() {
        return new WorldBorderSizePacket(diameter);
    }

    /**
     * Creates a {@link WorldBorderWarningDelayPacket} with this world border's warning time
     *
     * @return the {@link WorldBorderWarningDelayPacket} with this world border's warning time
     */
    public WorldBorderWarningDelayPacket createWarningDelayPacket() {
        return new WorldBorderWarningDelayPacket(warningTime);
    }

    /**
     * Creates a {@link WorldBorderWarningReachPacket} with this world border's warning distance
     *
     * @return the {@link WorldBorderWarningReachPacket} with this world border's warning distance
     */
    public WorldBorderWarningReachPacket createWarningReachPacket() {
        return new WorldBorderWarningReachPacket(warningDistance);
    }
}
