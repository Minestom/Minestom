package net.minestom.server.network.plugin;

import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public class LoginPluginMessageProcessor {
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    private final Map<Integer, LoginPluginRequest> requestByMsgId = new ConcurrentHashMap<>();
    private final PlayerConnection connection;

    public LoginPluginMessageProcessor(@NotNull PlayerConnection connection) {
        this.connection = connection;
    }

    public @NotNull CompletableFuture<LoginPluginResponse> request(@NotNull String channel, byte @Nullable [] requestPayload) {
        LoginPluginRequest request = new LoginPluginRequest(channel, requestPayload);

        int messageId = getNextMessageId();
        requestByMsgId.put(messageId, request);
        connection.sendPacket(new LoginPluginRequestPacket(messageId, request.getChannel(), request.getRequestPayload()));

        return request.getResponseFuture();
    }

    public void handleResponse(int messageId, byte[] responseData) throws Exception {
        LoginPluginRequest request = requestByMsgId.remove(messageId);
        if (request == null) {
            throw new Exception("Received unexpected Login Plugin Response id " + messageId + " of " + responseData.length + " bytes");
        }

        try {
            LoginPluginResponse response = LoginPluginResponse.fromPayload(request.getChannel(), responseData);
            request.getResponseFuture().complete(response);
        } catch (Throwable t) {
            throw new Exception("Error handling Login Plugin Response on channel '" + request.getChannel() + "'", t);
        }
    }

    public void awaitReplies(long timeout, @NotNull TimeUnit timeUnit) throws Exception {
        if (requestByMsgId.isEmpty()) {
            return;
        }

        CompletableFuture[] futures = requestByMsgId.values().stream()
                .map(LoginPluginRequest::getResponseFuture)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).get(timeout, timeUnit);
    }
    
    private static int getNextMessageId() {
        return REQUEST_ID.getAndIncrement();
    }
}
