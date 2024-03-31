package net.minestom.server.network.plugin;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.login.LoginPluginRequestPacket;
import net.minestom.server.network.player.PlayerSocketConnection;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginPluginMessageBox {
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(ThreadLocalRandom.current().nextInt());

    private final PlayerSocketConnection connection;

    private final Map<Integer, LoginPluginRequest> requestByMsgId = new ConcurrentHashMap<>();

    public LoginPluginMessageBox(PlayerSocketConnection connection) {
        this.connection = connection;
    }

    public CompletableFuture<LoginPluginResponse> request(String channel, byte[] requestPayload) {
        return request(new LoginPluginRequest(channel, requestPayload));
    }

    public CompletableFuture<LoginPluginResponse> request(LoginPluginRequest request) {
        ConnectionState connState = connection.getConnectionState();
        if (connState != ConnectionState.LOGIN) {
            return CompletableFuture.failedFuture(new IllegalStateException("Invalid connection state " + connState));
        }

        int messageId = getNextMessageId();

        connection.addPluginRequestEntry(messageId, request.getChannel());
        requestByMsgId.put(messageId, request);

        connection.sendPacket(new LoginPluginRequestPacket(messageId, request.getChannel(), request.getRequestPayload()));

        return request.getResponseFuture();
    }

    public void handle(int messageId, String channel, byte[] responseData) throws Exception {
        LoginPluginRequest request = requestByMsgId.remove(messageId);
        if (request == null) {
            throw new Exception("Received unexpected Login Plugin Response id " + messageId + " of " + responseData.length + " bytes");
        }

        if (!channel.equals(request.getChannel())) {
            throw new Exception("Channel mismatch between local '" + channel + "' and remote '" + request.getChannel() + "' for Login Plugin Response id " + messageId);
        }

        try {
            LoginPluginResponse response = LoginPluginResponse.fromPayload(channel, responseData);
            request.getResponseFuture().complete(response);
        } catch (Throwable t) {
            throw new Exception("Error handling Login Plugin Response on channel '" + channel + "'", t);
        }
    }
    
    public CompletableFuture<Void> getFutureForAllReplies() {
        if (requestByMsgId.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture[] futures = requestByMsgId.values().stream()
                .map(LoginPluginRequest::getResponseFuture)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }
    
    public void clear() {
        requestByMsgId.clear();
    }

    public static int getNextMessageId() {
        return REQUEST_ID.getAndIncrement();
    }
}
