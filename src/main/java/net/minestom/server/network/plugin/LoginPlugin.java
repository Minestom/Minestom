package net.minestom.server.network.plugin;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class LoginPlugin {
    public record Request(String channel, byte[] payload, CompletableFuture<Response> responseFuture) {
        public Request {
            Objects.requireNonNull(channel);
            Objects.requireNonNull(payload);
            Objects.requireNonNull(responseFuture);
            payload = payload.clone();
        }

        public Request(String channel, byte[] requestPayload) {
            this(channel, requestPayload, new CompletableFuture<>());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Request(String channel1, byte[] payload1, CompletableFuture<Response> future))) return false;
            return channel().equals(channel1) && Arrays.equals(payload(), payload1) && responseFuture().equals(future);
        }

        @Override
        public int hashCode() {
            int result = channel().hashCode();
            result = 31 * result + Arrays.hashCode(payload());
            result = 31 * result + responseFuture().hashCode();
            return result;
        }
    }

    public record Response(String channel, byte @Nullable [] payload) {
        public Response {
            Objects.requireNonNull(channel);
            payload = payload != null ? payload.clone() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Response(String channel1, byte[] payload1))) return false;
            return channel().equals(channel1) && Arrays.equals(payload(), payload1);
        }

        @Override
        public int hashCode() {
            int result = channel().hashCode();
            result = 31 * result + Arrays.hashCode(payload());
            return result;
        }
    }
}
