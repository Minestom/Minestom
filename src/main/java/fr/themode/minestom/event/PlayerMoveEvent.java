package fr.themode.minestom.event;

import fr.themode.minestom.utils.Position;

public class PlayerMoveEvent extends CancellableEvent {

    private Position newPosition;

    public PlayerMoveEvent(float x, float y, float z, float yaw, float pitch) {
        this.newPosition = new Position(x, y, z, yaw, pitch);
    }

    public Position getNewPosition() {
        return newPosition;
    }
}
