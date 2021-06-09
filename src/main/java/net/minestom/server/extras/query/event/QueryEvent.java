package net.minestom.server.extras.query.event;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * An event called when a query is received and ready to be responded to.
 *
 * @param <T> the type of the response
 */
public abstract class QueryEvent<T extends Writeable> implements CancellableEvent {
    private final SocketAddress sender;
    private final int sessionID;

    private T response;
    private boolean cancelled;

    /**
     * Creates a new query event.
     *
     * @param sender    the sender
     * @param sessionID the session ID of the query sender
     * @param response  the initial response
     */
    public QueryEvent(@NotNull SocketAddress sender, int sessionID, @NotNull T response) {
        this.sender = sender;
        this.sessionID = sessionID;
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
     * Gets the socket address of the initiator of the query.
     *
     * @return the initiator
     */
    public @NotNull SocketAddress getSender() {
        return this.sender;
    }

    /**
     * Gets the Session ID of the initiator of the query.
     *
     * @return the session ID
     */
    public int getSessionID() {
        return this.sessionID;
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
