package fr.themode.minestom.event;

public class BlockPlaceEvent extends CancellableEvent {

    private short blockId;

    public BlockPlaceEvent(short blockId) {
        this.blockId = blockId;
    }

    public short getBlockId() {
        return blockId;
    }
}
