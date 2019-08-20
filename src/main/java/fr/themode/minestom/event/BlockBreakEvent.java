package fr.themode.minestom.event;

import fr.themode.minestom.utils.Position;

public class BlockBreakEvent extends CancellableEvent {

    private Position position;

    public BlockBreakEvent(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
