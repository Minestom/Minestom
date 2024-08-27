package net.minestom.server.network.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class LoginPlugin {
    public record Request(String channel, byte @NotNull [] payload, CompletableFuture<Response> responseFuture) {
        public Request {
            Objects.requireNonNull(channel);
            Objects.requireNonNull(payload);
            Objects.requireNonNull(responseFuture);
        }

        public Request(String channel, byte @NotNull [] requestPayload) {
            this(channel, requestPayload, new CompletableFuture<>());
        }
    }

    public record Response(String channel, boolean understood, byte @NotNull [] payload) {
        public Response {
            Objects.requireNonNull(channel);
            Objects.requireNonNull(payload);
        }

        public static Response fromPayload(String channel, byte @NotNull [] payload) {
            final boolean understood = payload.length > 0;
            return new Response(channel, understood, payload);
        }
    }
}
