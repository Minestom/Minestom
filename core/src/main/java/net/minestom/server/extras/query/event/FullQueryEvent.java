package net.minestom.server.extras.query.event;

import net.minestom.server.extras.query.response.FullQueryResponse;
import org.jetbrains.annotations.NotNull;

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
    public FullQueryEvent(@NotNull SocketAddress sender, int sessionID) {
        super(sender, sessionID, new FullQueryResponse());
    }
}
