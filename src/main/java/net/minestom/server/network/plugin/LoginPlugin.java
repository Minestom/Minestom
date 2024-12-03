package net.minestom.server.network.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public record Response(String channel, byte @Nullable [] payload) {
        public Response {
            Objects.requireNonNull(channel);
        }
    }
}
