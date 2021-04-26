package net.minestom.server.extras.query.event;

import net.minestom.server.extras.query.response.BasicQueryResponse;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

/**
 * An event called when a basic query is received and ready to be responded to.
 */
public class BasicQueryEvent extends QueryEvent<BasicQueryResponse> {

    /**
     * Creates a new basic query event.
     *
     * @param sender the sender
     */
    public BasicQueryEvent(@NotNull SocketAddress sender) {
        super(sender, new BasicQueryResponse());
    }
}
