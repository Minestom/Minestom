package net.minestom.server.network.plugin;

public class LoginPluginResponse {
    private final String channel;
    private final boolean understood;
    private final byte[] payload;

    private LoginPluginResponse(String channel, boolean understood, byte[] payload) {
        this.channel = channel;
        this.understood = understood;
        this.payload = payload;
    }

    public String getChannel() {
        return channel;
    }

    public boolean isUnderstood() {
        return understood;
    }

    public byte[] getPayload() {
        return payload;
    }

    public static LoginPluginResponse fromPayload(String channel, byte[] payload) {
        boolean understood = payload != null;
        return new LoginPluginResponse(channel, understood, payload);
    }
}
