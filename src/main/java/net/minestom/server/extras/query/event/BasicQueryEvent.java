package net.minestom.server.extras.query.event;

import net.minestom.server.extras.query.response.BasicQueryResponse;
import net.minestom.server.utils.InetAddressWithPort;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when a basic query is received and ready to be responded to.
 */
public class BasicQueryEvent extends QueryEvent<BasicQueryResponse> {

    /**
     * Creates a new basic query event.
     *
     * @param sender the sender
     */
    public BasicQueryEvent(@NotNull InetAddressWithPort sender) {
        super(sender, new BasicQueryResponse());
    }
}
