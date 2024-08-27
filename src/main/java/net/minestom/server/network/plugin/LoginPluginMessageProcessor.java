package net.minestom.server.network.plugin;

import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public class LoginPluginMessageProcessor {
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    private final Map<Integer, LoginPlugin.Request> requestByMsgId = new ConcurrentHashMap<>();
    private final PlayerConnection connection;

    public LoginPluginMessageProcessor(@NotNull PlayerConnection connection) {
        this.connection = connection;
    }

    public @NotNull CompletableFuture<LoginPlugin.Response> request(@NotNull String channel, byte @NotNull [] requestPayload) {
        LoginPlugin.Request request = new LoginPlugin.Request(channel, requestPayload);

        final int messageId = nextMessageId();
        requestByMsgId.put(messageId, request);
        connection.sendPacket(new LoginPluginRequestPacket(messageId, request.channel(), request.payload()));

        return request.responseFuture();
    }

    public void handleResponse(int messageId, byte[] responseData) throws Exception {
        LoginPlugin.Request request = requestByMsgId.remove(messageId);
        if (request == null) {
            throw new Exception("Received unexpected Login Plugin Response id " + messageId + " of " + responseData.length + " bytes");
        }

        try {
            LoginPlugin.Response response = new LoginPlugin.Response(request.channel(), responseData);
            request.responseFuture().complete(response);
        } catch (Throwable t) {
            throw new Exception("Error handling Login Plugin Response on channel '" + request.channel() + "'", t);
        }
    }

    public void awaitReplies(long timeout, @NotNull TimeUnit timeUnit) throws Exception {
        if (requestByMsgId.isEmpty()) {
            return;
        }
        CompletableFuture[] futures = requestByMsgId.values().stream()
                .map(LoginPlugin.Request::responseFuture)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).get(timeout, timeUnit);
    }

    private static int nextMessageId() {
        return REQUEST_ID.getAndIncrement();
    }
}
