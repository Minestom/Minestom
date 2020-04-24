package net.minestom.server.event;

public class PlayerPluginMessageEvent extends Event {

    private String identifier;
    private byte[] message;

    public PlayerPluginMessageEvent(String identifier, byte[] message) {
        this.identifier = identifier;
        this.message = message;
    }

    public String getIdentifier() {
        return identifier;
    }

    public byte[] getMessage() {
        return message;
    }

    public String getMessageString() {
        return new String(message);
    }
}
