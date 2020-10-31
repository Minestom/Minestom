package net.minestom.server.listener.manager;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import org.jetbrains.annotations.Nullable;

/**
 * Used to control the output of a packet in {@link PacketConsumer#accept(Player, PacketController, ClientPlayPacket)}.
 */
public class PacketController {

    private boolean cancel;
    private PacketListenerConsumer packetListenerConsumer;

    protected PacketController(@Nullable PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
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

    /**
     * Gets the listener associated with the packet.
     *
     * @return the packet's listener, null if not present
     */
    @Nullable
    public PacketListenerConsumer getPacketListenerConsumer() {
        return packetListenerConsumer;
    }

    /**
     * Changes the packet listener, setting it to null cancel the listener.
     * <p>
     * WARNING: this will overwrite the default minestom listener.
     *
     * @param packetListenerConsumer the new packet listener, can be null
     */
    public void setPacketListenerConsumer(@Nullable PacketListenerConsumer packetListenerConsumer) {
        this.packetListenerConsumer = packetListenerConsumer;
    }
}
