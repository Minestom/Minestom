package fr.themode.minestom.entity;

import fr.themode.minestom.Main;
import fr.themode.minestom.net.packet.server.play.EntityTeleportPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.UUID;

public class Player extends LivingEntity {

    private boolean isSneaking;
    private boolean isSprinting;

    private long lastKeepAlive;

    private String username;
    private PlayerConnection playerConnection;

    // TODO set proper UUID
    public Player(UUID uuid, String username, PlayerConnection playerConnection) {
        this.uuid = uuid;
        this.username = username;
        this.playerConnection = playerConnection;
    }

    @Override
    public void update() {
        // System.out.println("Je suis l'update");
        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.x = x;
        entityTeleportPacket.y = y;
        entityTeleportPacket.z = z;
        entityTeleportPacket.yaw = yaw;
        entityTeleportPacket.pitch = pitch;
        entityTeleportPacket.onGround = true;
        for (Player onlinePlayer : Main.getConnectionManager().getOnlinePlayers()) {
            if (!onlinePlayer.equals(this))
                onlinePlayer.getPlayerConnection().sendPacket(entityTeleportPacket);
        }
    }

    public String getUsername() {
        return username;
    }

    public PlayerConnection getPlayerConnection() {
        return playerConnection;
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

    public enum Hand {
        MAIN,
        OFF
    }
}
