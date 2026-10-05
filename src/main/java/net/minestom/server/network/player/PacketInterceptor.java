package net.minestom.server.network.player;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;

import org.jetbrains.annotations.Nullable;

/**
 * Intercepts raw packet frames for a connection.
 * <p>
 * Incoming frames are exposed by {@link #preRead} before the packet object is decoded.
 * Outgoing frames are exposed by {@link #postWrite} after the packet object was serialized.
 * Unfiltered packets take the vanilla path without overhead.
 */
public interface PacketInterceptor {

    /**
     * Decides what happens to the current frame.
     */
    sealed interface Verdict {
        /**
         * Forwards the frame untouched.
         * <p>
         * In-place edits to the given frame are included, no copy happens.
         */
        record Keep() implements Verdict {
        }

        /**
         * Skips the packet entirely, as if it was never received or sent.
         */
        record Drop() implements Verdict {
        }

        /**
         * Parses or writes the given frame instead of the original.
         *
         * @param frame the replacement frame, or null to use the frame passed to the hook
         */
        record Replace(@Nullable NetworkBuffer frame) implements Verdict {
        }
    }

    /**
     * Filters incoming packets by id, before any decoding.
     *
     * @param state the current connection state
     * @param packetId the peeked packet id
     * @return true to invoke {@link #preRead}, false for the vanilla path
     */
    default boolean interceptsIncoming(ConnectionState state, int packetId) {
        return true;
    }

    /**
     * Filters outgoing packets by object, before serialization.
     *
     * @param state the current connection state
     * @param packet the packet about to be written
     * @return true to invoke {@link #postWrite}, false for the vanilla path
     */
    default boolean interceptsOutgoing(ConnectionState state, ServerPacket packet) {
        return true;
    }

    /**
     * Transforms an incoming frame.
     *
     * @param connection the owning connection
     * @param state the connection state the frame was received in
     * @param frame the mutable {@code [packetId + payload]} slice
     * @return how to continue with the frame
     */
    default Verdict preRead(PlayerSocketConnection connection, ConnectionState state, NetworkBuffer frame) {
        return new Verdict.Keep();
    }

    /**
     * Transforms an outgoing frame.
     * <p>
     * In-place edits must preserve the frame length, return {@link Verdict.Replace}
     * otherwise.
     *
     * @param connection the owning connection
     * @param state the connection state the packet is written in
     * @param packet the packet being written
     * @param frame the mutable serialized {@code [packetId + payload]} slice
     * @return how to continue with the frame
     */
    default Verdict postWrite(PlayerSocketConnection connection, ConnectionState state,
                              ServerPacket packet, NetworkBuffer frame) {
        return new Verdict.Keep();
    }
}
