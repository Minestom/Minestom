package net.minestom.server.listener.manager;

public class PacketController {

    private boolean cancel;
    private PacketListenerConsumer packetListenerConsumer;

    protected PacketController(PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
    }

    /**
     * Get if the packet is cancelled
     *
     * @return true if the packet will be cancelled, false otherwise
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * Used to cancel the packet
     *
     * @param cancel true if the packet should be cancelled, false otherwise
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Get the listener associated with the packet
     *
     * @return the packet's listener
     */
    public PacketListenerConsumer getPacketListenerConsumer() {
        return packetListenerConsumer;
    }

    /**
     * Change the packet listener, setting it to null cancel the listener
     * <p>
     * WARNING: this will overwrite the default minestom listener
     *
     * @param packetListenerConsumer the new packet listener
     */
    public void setPacketListenerConsumer(PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
    }
}
