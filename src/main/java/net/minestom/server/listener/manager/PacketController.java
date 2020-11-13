package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;

/**
 * Used to control the output of a packet in {@link PacketConsumer#accept(Player, PacketController, Object)}.
 */
public class PacketController {

    private boolean cancel;

    protected PacketController() {
    }

    /**
     * Gets if the packet is cancelled.
     *
     * @return true if the packet will be cancelled, false otherwise
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * Used to cancel the packet.
     *
     * @param cancel true if the packet should be cancelled, false otherwise
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
