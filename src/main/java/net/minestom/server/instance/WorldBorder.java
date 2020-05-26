package net.minestom.server.instance;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.WorldBorderPacket;
import net.minestom.server.utils.Position;

public class WorldBorder {

    private Instance instance;

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

    public void setCenter(float centerX, float centerZ) {
        this.centerX = centerX;
        this.centerZ = centerZ;
        refreshCenter();
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
        refreshCenter();
    }

    public double getCenterZ() {
        return centerZ;
    }

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
     * @return the current world border diameter
     * It takes lerp in consideration
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
     * Used to know if a position is located inside the world border or not
     *
     * @param position the position to check
     * @return true if {@code position} is inside the world border, false otherwise
     */
    public boolean isInside(Position position) {
        final double radius = getDiameter() / 2d;
        final boolean checkX = position.getX() < getCenterX() + radius && position.getX() > getCenterX() - radius;
        if (!checkX)
            return false;
        final boolean checkZ = position.getZ() < getCenterZ() + radius && position.getZ() > getCenterZ() - radius;
        return checkZ;
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
     */
    protected void update() {
        if (lerpStartTime == 0) {
            this.currentDiameter = oldDiameter;
        } else {
            double diameterDelta = newDiameter - oldDiameter;
            long elapsedTime = System.currentTimeMillis() - lerpStartTime;
            this.currentDiameter = oldDiameter + (diameterDelta * ((double) elapsedTime / (double) speed));
        }
    }

    protected void init(Player player) {
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.INITIALIZE;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBInitialize(centerX, centerZ, oldDiameter, newDiameter, speed,
                portalTeleportBoundary, warningTime, warningBlocks);
        player.getPlayerConnection().sendPacket(worldBorderPacket);
    }

    /**
     * @return the instance of this world border
     */
    public Instance getInstance() {
        return instance;
    }

    private void refreshCenter() {
        WorldBorderPacket worldBorderPacket = new WorldBorderPacket();
        worldBorderPacket.action = WorldBorderPacket.Action.SET_CENTER;
        worldBorderPacket.wbAction = new WorldBorderPacket.WBSetCenter(centerX, centerZ);
        sendPacket(worldBorderPacket);
    }

    private void sendPacket(WorldBorderPacket worldBorderPacket) {
        instance.getPlayers().forEach(player -> player.getPlayerConnection().sendPacket(worldBorderPacket));
    }
}
