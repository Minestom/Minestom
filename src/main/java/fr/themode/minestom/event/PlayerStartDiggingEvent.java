package fr.themode.minestom.event;

import fr.themode.minestom.instance.CustomBlock;

public class PlayerStartDiggingEvent extends CancellableEvent {

    private CustomBlock customBlock;

    public PlayerStartDiggingEvent(CustomBlock customBlock) {
        this.customBlock = customBlock;
    }

    public CustomBlock getBlock() {
        return customBlock;
    }
}
