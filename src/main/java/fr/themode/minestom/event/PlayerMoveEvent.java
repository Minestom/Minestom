package fr.themode.minestom.event;

public class PlayerMoveEvent extends CancellableEvent {

    private float toX, toY, toZ;

    public PlayerMoveEvent(float toX, float toY, float toZ) {
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
    }

}
