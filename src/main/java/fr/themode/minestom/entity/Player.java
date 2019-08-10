package fr.themode.minestom.entity;

import fr.themode.minestom.net.player.PlayerConnection;

public class Player {

    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    private PlayerConnection playerConnection;

    public PlayerConnection getPlayerConnection() {
        return playerConnection;
    }

    public void refreshPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void refreshView(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void refreshOnGround(boolean onGround) {
        this.onGround = onGround;
    }

}
