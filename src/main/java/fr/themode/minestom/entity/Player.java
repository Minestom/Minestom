package fr.themode.minestom.entity;

import fr.themode.minestom.net.player.PlayerConnection;

public class Player extends LivingEntity {

    private boolean isSneaking;
    private boolean isSprinting;

    private long lastKeepAlive;

    private PlayerConnection playerConnection;

    // TODO set proper UUID
    public Player(PlayerConnection playerConnection) {
        this.playerConnection = playerConnection;
    }

    @Override
    public void update() {
        //System.out.println("Je suis l'update");
    }

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

    public void refreshSneaking(boolean sneaking) {
        isSneaking = sneaking;
    }

    public void refreshSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    public void refreshKeepAlive(long lastKeepAlive) {
        this.lastKeepAlive = lastKeepAlive;
    }

    public long getLastKeepAlive() {
        return lastKeepAlive;
    }
}
