package net.minestom.server.extras.query.event;

import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

/**
 * An event called when a query is received and ready to be responded to.
 *
 * @param <R> the type of the response
 */
public interface QueryEvent<R, G extends QueryEvent<R, G>> extends CancellableEvent<G> {

    /**
     * Gets the query response that will be sent back to the sender.
     * This can be mutated.
     *
     * @return the response
     */
    R queryResponse();

    /**
     * Gets the socket address of the initiator of the query.
     *
     * @return the initiator
     */
    @NotNull SocketAddress sender();

    /**
     * Gets the Session ID of the initiator of the query.
     *
     * @return the session ID
     */
    int sessionId();
}
