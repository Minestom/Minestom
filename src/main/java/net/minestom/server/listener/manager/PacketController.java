package net.minestom.server.listener.manager;

public class PacketController {

    private boolean cancel;
    private PacketListenerConsumer packetListenerConsumer;

    protected PacketController(PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
    }

    public boolean isCancel() {
        return cancel;
    }

    /**
     * @param cancel true if the packet should be cancelled
     */
    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public PacketListenerConsumer getPacketListenerConsumer() {
        return packetListenerConsumer;
    }

    /**
     * @param packetListenerConsumer the new listener (do not override the default listener)
     */
    public void setPacketListenerConsumer(PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
    }
}
