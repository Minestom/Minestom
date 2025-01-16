package net.minestom.server.extras.query.event;

import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.extras.query.response.BasicQueryResponse;
import net.minestom.server.extras.query.response.FullQueryResponse;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * An event called when a basic query is received and ready to be responded to.
 */
public record BasicQueryEvent(@NotNull SocketAddress sender, int sessionID, @NotNull BasicQueryResponse response, boolean cancelled) implements QueryEvent<BasicQueryResponse, BasicQueryEvent> {

    /**
     * Creates a new full query event.
     *
     * @param sender the sender
     * @param sessionID the sessionID
     */
    public BasicQueryEvent(@NotNull SocketAddress sender, int sessionID) {
        this(sender, sessionID, new BasicQueryResponse(), false);
    }
    /**
     * Gets the query response that will be sent back to the sender.
     * This can be mutated.
     *
     * @return the response
     */
    @Override
    public BasicQueryResponse queryResponse() {
        return this.response;
    }

    /**
     * Gets the socket address of the initiator of the query.
     *
     * @return the initiator
     */
    public @NotNull SocketAddress sender() {
        return this.sender;
    }

    /**
     * Gets the Session ID of the initiator of the query.
     *
     * @return the session ID
     */
    public int sessionId() {
        return this.sessionID;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<BasicQueryEvent> {
        private final SocketAddress sender;
        private final int sessionID;

        private BasicQueryResponse response;
        private boolean cancelled;

        public Mutator(BasicQueryEvent event) {
            this.sender = event.sender;
            this.sessionID = event.sessionID;
            this.response = event.response;
            this.cancelled = event.cancelled;
        }


        /**
         * Gets the query response that will be sent back to the sender.
         * This can be mutated.
         *
         * @return the response
         */
        public BasicQueryResponse getQueryResponse() {
            return this.response;
        }

        /**
         * Sets the query response that will be sent back to the sender.
         *
         * @param response the response
         */
        public void setQueryResponse(@NotNull BasicQueryResponse response) {
            this.response = Objects.requireNonNull(response, "response");
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull BasicQueryEvent mutated() {
            return new BasicQueryEvent(this.sender, this.sessionID, this.response, this.cancelled);
        }
    }
}