package net.minestom.server.event.player;

import net.minestom.server.event.Event;
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
