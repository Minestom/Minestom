package fr.themode.minestom.event;

import fr.themode.minestom.instance.CustomBlock;

public class StartDiggingEvent extends CancellableEvent {

    private CustomBlock customBlock;

    public StartDiggingEvent(CustomBlock customBlock) {
        this.customBlock = customBlock;
    }

    public CustomBlock getBlock() {
        return customBlock;
    }
}
