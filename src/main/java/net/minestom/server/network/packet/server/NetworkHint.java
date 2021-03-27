package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.NotNull;

public class NetworkHint {

    public static NetworkHint Preservative() {
        return new NetworkHint(WriteType.BUFFER, -1);
    }

    public static NetworkHint Unordered(int priority) {
        return new NetworkHint(WriteType.GROUPED, priority);
    }

    private final WriteType writeType;
    private final int priority;

    /**
     * Creates a new network hint.
     *
     * @param writeType how the packet is written
     * @param priority  the priority to ensure that packet are sent in the right order
     */
    private NetworkHint(@NotNull WriteType writeType, int priority) {
        this.writeType = writeType;
        this.priority = priority;
    }

    @NotNull
    public WriteType getWriteType() {
        return writeType;
    }

    public int getPriority() {
        return priority;
    }

    public enum WriteType {
        /**
         * Packet is directly sent using {@link io.netty.channel.Channel#write(Object)}.
         */
        DIRECT,
        /**
         * Packet is stored inside player-specific buffer.
         */
        BUFFER,
        /**
         * Packet is cached inside a buffer to be reused with multiple connections.
         */
        GROUPED;
    }

}
