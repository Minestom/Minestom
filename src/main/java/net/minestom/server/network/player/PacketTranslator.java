package net.minestom.server.network.player;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;

/**
 * Translates raw packet frames for a connection.
 * <p>
 * Incoming frames are translated before the packet objects are decoded. Outgoing
 * frames are translated after the packet objects were serialized. Unfiltered
 * packets take the vanilla path without overhead.
 * <p>
 * Several translators may coexist on one connection, applied in registration
 * order with each observing the previous output.
 */
public interface PacketTranslator {

    /**
     * Filters incoming packets by id, before any decoding.
     *
     * @param state the current connection state
     * @param packetId the peeked packet id
     * @return true to invoke {@link #translateIncoming}, false for the vanilla path
     */
    default boolean translatesIncoming(ConnectionState state, int packetId) {
        return true;
    }

    /**
     * Filters outgoing packets by object, before serialization.
     *
     * @param state the current connection state
     * @param packet the packet about to be written
     * @return true to invoke {@link #translateOutgoing}, false for the vanilla path
     */
    default boolean translatesOutgoing(ConnectionState state, ServerPacket packet) {
        return true;
    }

    /**
     * Translates incoming frames.
     * <p>
     * Emitted frames are decoded in order with chained states. Frames that are
     * never returned are skipped.
     *
     * @param connection the owning connection
     * @param state the connection state the frames were received in
     * @param frames the incoming frames
     * @return the translated frames, in order
     */
    default List<NetworkBuffer> translateIncoming(PlayerSocketConnection connection, ConnectionState state,
                                                  List<NetworkBuffer> frames) {
        return frames;
    }

    /**
     * Translates outgoing frames.
     * <p>
     * In-place edits must preserve the frame length.
     *
     * @param connection the owning connection
     * @param state the connection state the packets are written in
     * @param packet the packet being written
     * @param frames the serialized frames
     * @return the translated frames, in order
     */
    default List<NetworkBuffer> translateOutgoing(PlayerSocketConnection connection, ConnectionState state,
                                                  ServerPacket packet, List<NetworkBuffer> frames) {
        return frames;
    }
}
