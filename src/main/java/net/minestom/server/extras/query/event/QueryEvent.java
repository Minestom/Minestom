package net.minestom.server.extras.query.event;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.extras.query.response.QueryResponse;
import net.minestom.server.utils.InetAddressWithPort;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An event called when a query is received and ready to be responded to.
 *
 * @param <T> the type of the response
 */
public abstract class QueryEvent<T extends QueryResponse> extends Event implements CancellableEvent {
    private final InetAddressWithPort sender;

    private T response;
    private boolean cancelled;

    /**
     * Creates a new query event.
     *
     * @param sender the sender
     * @param response the initial response
     */
    public QueryEvent(@NotNull InetAddressWithPort sender, @NotNull T response) {
        this.sender = sender;
        this.response = response;
        this.cancelled = false;
    }

    /**
     * Gets the query response that will be sent back to the sender.
     * This can be mutated.
     *
     * @return the response
     */
    public T getQueryResponse() {
        return this.response;
    }

    /**
     * Sets the query response that will be sent back to the sender.
     *
     * @param response the response
     */
    public void setQueryResponse(@NotNull T response) {
        this.response = Objects.requireNonNull(response, "response");
    }

    /**
     * Gets the IP address and port of the initiator of the query.
     *
     * @return the initiator
     */
    public @NotNull InetAddressWithPort getSender() {
        return this.sender;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
