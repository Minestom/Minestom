package net.minestom.server.extras.query.event;

import net.minestom.server.extras.query.response.FullQueryResponse;
import net.minestom.server.utils.InetAddressWithPort;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when a full query is received and ready to be responded to.
 */
public class FullQueryEvent extends QueryEvent<FullQueryResponse> {

    /**
     * Creates a new full query event.
     *
     * @param sender the sender
     */
    public FullQueryEvent(@NotNull InetAddressWithPort sender) {
        super(sender, new FullQueryResponse());
    }
}
