package net.minestom.server.item.drop;

/**
 * The item was dropped from the hotbar (default key: q)
 */
public class DropReasonHotbar extends DropReason {
    private final int slot;

    public DropReasonHotbar(int slot) {
        this.slot = slot;
    }


    public int getSlot() {
        return slot;
    }
}
