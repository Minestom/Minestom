package fr.themode.minestom.event;

import fr.themode.minestom.utils.Position;

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
