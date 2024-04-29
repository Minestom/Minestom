package net.minestom.server.network.plugin;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class LoginPluginRequest {
    private final String channel;
    private final byte[] requestPayload;
    private final CompletableFuture<LoginPluginResponse> responseFuture = new CompletableFuture<>();

    public LoginPluginRequest(String channel, @Nullable byte[] requestPayload) {
        this.channel = channel;
        this.requestPayload = requestPayload;
    }

    public String getChannel() {
        return channel;
    }

    public @Nullable byte[] getRequestPayload() {
        return requestPayload;
    }

    public CompletableFuture<LoginPluginResponse> getResponseFuture() {
        return responseFuture;
    }
}
