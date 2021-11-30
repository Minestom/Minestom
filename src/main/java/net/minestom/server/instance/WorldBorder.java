package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the world border of an {@link Instance},
 * can be retrieved with {@link Instance#getWorldBorder()}.
 */
public class WorldBorder {

    private final Instance instance;

    private float centerX, centerZ;

    private volatile double currentDiameter;

    private double oldDiameter;
    private double newDiameter;

    private long lerpStartTime;

    private long speed;
    private int portalTeleportBoundary;
    private int warningTime;
    private int warningBlocks;

    protected WorldBorder(@NotNull Instance instance) {
        this.instance = instance;

        this.oldDiameter = Double.MAX_VALUE;
        this.newDiameter = Double.MAX_VALUE;

        this.speed = 0;

        this.portalTeleportBoundary = 29999984;

    }

    /**
     * Changes the X and Z position of the center.
     *
     * @param centerX the X center
     * @param centerZ the Z center
     */
    public void setCenter(float centerX, float centerZ) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        refreshCenter();
    }

    /**
     * Gets the center X of the world border.
     *
     * @return the X center
     */
    public float getCenterX() {
        return centerX;
    }

    /**
     * Changes the center X of the world border.
     *
     * @param centerX the new center X
     */
    public void setCenterX(float centerX) {
        this.centerX = centerX;
        refreshCenter();
    }

    /**
     * Gets the center Z of the world border.
     *
     * @return the Z center
     */
    public float getCenterZ() {
        return centerZ;
    }

    /**
     * Changes the center Z of the world border.
     *
     * @param centerZ the new center Z
     */
    public void setCenterZ(float centerZ) {
        this.centerZ = centerZ;
        refreshCenter();
    }

    public int getWarningTime() {
        return warningTime;
    }

    /**
     * @param warningTime In seconds as /worldborder warning time
     */
    public void setWarningTime(int warningTime) {
        this.warningTime = warningTime;
        sendPacket(new WorldBorderWarningDelayPacket(warningTime));
    }

    public int getWarningBlocks() {
        return warningBlocks;
    }

    /**
     * @param warningBlocks In meters
     */
    public void setWarningBlocks(int warningBlocks) {
        this.warningBlocks = warningBlocks;
        sendPacket(new WorldBorderWarningReachPacket(warningBlocks));
    }

    /**
     * Changes the diameter to {@code diameter} in {@code speed} milliseconds (interpolation).
     *
     * @param diameter the diameter target
     * @param speed    the time it will take to reach {@code diameter} in milliseconds
     */
    public void setDiameter(double diameter, long speed) {
        if (speed <= 0) {
            setDiameter(diameter);
            return;
        }
        this.newDiameter = diameter;
        this.speed = speed;
        this.lerpStartTime = System.currentTimeMillis();
        sendPacket(new WorldBorderLerpSizePacket(oldDiameter, newDiameter, speed));
    }

    /**
     * Gets the diameter of the world border.
     * It takes lerp in consideration.
     *
     * @return the current world border diameter
     */
    public double getDiameter() {
        return currentDiameter;
    }

    /**
     * Changes the diameter of the world border.
     *
     * @param diameter the new diameter of the world border
     */
    public void setDiameter(double diameter) {
        this.currentDiameter = diameter;
        this.oldDiameter = diameter;
        this.newDiameter = diameter;
        this.lerpStartTime = 0;
        sendPacket(new WorldBorderSizePacket(diameter));
    }

    /**
     * Used to check at which axis does the position collides with the world border.
     *
     * @param point the point to check
     * @return the axis where the position collides with the world border
     */
    public @NotNull CollisionAxis getCollisionAxis(@NotNull Point point) {
        final double radius = getDiameter() / 2d;
        final boolean checkX = point.x() <= getCenterX() + radius && point.x() >= getCenterX() - radius;
        final boolean checkZ = point.z() <= getCenterZ() + radius && point.z() >= getCenterZ() - radius;
        if (!checkX || !checkZ) {
            if (!checkX && !checkZ) {
                return CollisionAxis.BOTH;
            } else if (!checkX) {
                return CollisionAxis.X;
            } else {
                return CollisionAxis.Z;
            }
        }
        return CollisionAxis.NONE;
    }

    /**
     * Used to know if a position is located inside the world border or not.
     *
     * @param point the point to check
     * @return true if {@code position} is inside the world border, false otherwise
     */
    public boolean isInside(@NotNull Point point) {
        return getCollisionAxis(point) == CollisionAxis.NONE;
    }

    /**
     * Used to know if an entity is located inside the world border or not.
     *
     * @param entity the entity to check
     * @return true if {@code entity} is inside the world border, false otherwise
     */
    public boolean isInside(@NotNull Entity entity) {
        return isInside(entity.getPosition());
    }

    /**
     * Used to update in real-time the current diameter time.
     * Called in the instance tick update.
     */
    protected void update() {
        if (lerpStartTime == 0) {
            this.currentDiameter = oldDiameter;
        } else {
            double diameterDelta = newDiameter - oldDiameter;
            long elapsedTime = System.currentTimeMillis() - lerpStartTime;
            double percentage = (double) elapsedTime / (double) speed;
            percentage = Math.max(percentage, 1);
            this.currentDiameter = oldDiameter + (diameterDelta * percentage);

            // World border finished lerp
            if (percentage == 1) {
                this.lerpStartTime = 0;
                this.speed = 0;
                this.oldDiameter = newDiameter;
            }
        }
    }

    /**
     * Sends the world border init packet to a player.
     *
     * @param player the player to send the packet to
     */
    @ApiStatus.Internal
    public void init(@NotNull Player player) {
        player.sendPacket(new InitializeWorldBorderPacket(centerX, centerZ,
                oldDiameter, newDiameter, speed, portalTeleportBoundary, warningTime, warningBlocks));
    }

    /**
     * Gets the {@link Instance} linked to this world border.
     *
     * @return the {@link Instance} of this world border
     */
    @NotNull
    public Instance getInstance() {
        return instance;
    }

    /**
     * Sends the new world border centers to all instance players.
     */
    private void refreshCenter() {
        sendPacket(new WorldBorderCenterPacket(centerX, centerZ));
    }

    private void sendPacket(@NotNull ServerPacket packet) {
        PacketUtils.sendGroupedPacket(instance.getPlayers(), packet);
    }

    public enum CollisionAxis {
        X, Z, BOTH, NONE
    }

}
