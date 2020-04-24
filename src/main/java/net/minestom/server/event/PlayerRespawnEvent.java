package net.minestom.server.event;

import net.minestom.server.utils.Position;

public class PlayerRespawnEvent extends Event {

    private Position respawnPosition;

    public PlayerRespawnEvent(Position respawnPosition) {
        this.respawnPosition = respawnPosition;
    }

    public Position getRespawnPosition() {
        return respawnPosition;
    }

    public void setRespawnPosition(Position respawnPosition) {
        this.respawnPosition = respawnPosition;
    }
}
