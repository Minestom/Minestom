package net.minestom.server.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.PacketWriterUtils;
import net.minestom.server.network.packet.server.play.WorldBorderPacket;
import net.minestom.server.utils.Position;

/**
 * Represent the world border of an instance
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

    protected WorldBorder(Instance instance) {
        this.instance = instance;

        this.oldDiameter = Double.MAX_VALUE;
        this.newDiameter = Double.MAX_VALUE;

        this.speed = 0;

        this.portalTeleportBoundary = 29999984;

    }

    /**
     * Change the X and Z position of the center
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
     * Get the center X of the world border
     *
     * @return the X center
     */
    public float getCenterX() {
        return centerX;
    }

    /**
     * Change the center X of the world border
     *
     * @param centerX the new center X
     */
    public void setCenterX(float centerX) {
        this.centerX = centerX;
        refreshCenter();
    }

    /**
     * Get the center Z of the world border
     *
     * @return the Z center
     */
    public float getCenterZ() {
        return centerZ;
    }

    /**
     * Change the center Z of the world border
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
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.SET_WARNING_TIME;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBSetWarningTime(warningTime);
        sendPacket(worldBorderPacket);
    }

    public int getWarningBlocks() {
        return warningBlocks;
    }

    /**
     * @param warningBlocks In meters
     */
    public void setWarningBlocks(int warningBlocks) {
        this.warningBlocks = warningBlocks;
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.SET_WARNING_BLOCKS;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBSetWarningBlocks(warningBlocks);
        sendPacket(worldBorderPacket);
    }

    /**
     * Change the diameter to {@code diameter} in {@code speed} milliseconds (interpolation)
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

        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.LERP_SIZE;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBLerpSize(oldDiameter, newDiameter, speed);
        sendPacket(worldBorderPacket);
    }

    /**
     * Get the diameter of the world border
     * It takes lerp in consideration
     *
     * @return the current world border diameter
     */
    public double getDiameter() {
        return currentDiameter;
    }

    /**
     * Change the diameter of the world border
     *
     * @param diameter the new diameter of the world border
     */
    public void setDiameter(double diameter) {
        this.currentDiameter = diameter;
        this.oldDiameter = diameter;
        this.newDiameter = diameter;
        this.lerpStartTime = 0;

        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.SET_SIZE;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBSetSize(diameter);
        sendPacket(worldBorderPacket);
    }

    /**
     * Used to check at which axis does the position collides with the world border
     *
     * @param position the position to check
     * @return the axis where the position collides with the world border
     */
    public CollisionAxis getCollisionAxis(Position position) {
        final double radius = getDiameter() / 2d;
        final boolean checkX = position.getX() <= getCenterX() + radius && position.getX() >= getCenterX() - radius;
        final boolean checkZ = position.getZ() <= getCenterZ() + radius && position.getZ() >= getCenterZ() - radius;
        if (!checkX && !checkZ) {
            return CollisionAxis.BOTH;
        } else if (!checkX) {
            return CollisionAxis.X;
        } else if (!checkZ) {
            return CollisionAxis.Z;
        }
        return CollisionAxis.NONE;
    }

    /**
     * Used to know if a position is located inside the world border or not
     *
     * @param position the position to check
     * @return true if {@code position} is inside the world border, false otherwise
     */
    public boolean isInside(Position position) {
        return getCollisionAxis(position) == CollisionAxis.NONE;
    }

    /**
     * Used to know if an entity is located inside the world border or not
     *
     * @param entity the entity to check
     * @return true if {@code entity} is inside the world border, false otherwise
     */
    public boolean isInside(Entity entity) {
        return isInside(entity.getPosition());
    }

    /**
     * Used to update in real-time the current diameter time
     * Called in the instance tick update
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
     * Send the world border init packet to a player
     *
     * @param player the player to send the packet to
     */
    protected void init(Player player) {
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.INITIALIZE;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBInitialize(centerX, centerZ, oldDiameter, newDiameter, speed,
                portalTeleportBoundary, warningTime, warningBlocks);
        player.getPlayerConnection().sendPacket(worldBorderPacket);
    }

    /**
     * Get the instance linked to this world border
     *
     * @return the instance of this world border
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * Send the new world border centers to all instance players
     */
    private void refreshCenter() {
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.SET_CENTER;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBSetCenter(centerX, centerZ);
        sendPacket(worldBorderPacket);
    }

    /**
     * Send a {@link WorldBorderPacket} to all the instance players
     *
     * @param worldBorderPacket the packet to send
     */
    private void sendPacket(WorldBorderPacket worldBorderPacket) {
        PacketWriterUtils.writeAndSend(instance.getPlayers(), worldBorderPacket);
    }

    public enum CollisionAxis {
        X, Z, BOTH, NONE
    }

}
