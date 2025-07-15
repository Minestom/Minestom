package net.minestom.server.extras.query.event;

import net.minestom.server.extras.query.response.FullQueryResponse;

import java.net.SocketAddress;

/**
 * An event called when a full query is received and ready to be responded to.
 */
public class FullQueryEvent extends QueryEvent<FullQueryResponse> {

    /**
     * Creates a new full query event.
     *
     * @param sender the sender
     * @param sessionID the sessionID
     */
    public FullQueryEvent(SocketAddress sender, int sessionID) {
        super(sender, sessionID, new FullQueryResponse());
    }
}
