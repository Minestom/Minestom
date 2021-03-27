package net.minestom.server.network.packet.server;

public class NetworkHint {

    public static final NetworkHint ORDER_UNAWARE_PACKET = new NetworkHint(false, false);

    private final boolean directWrite;
    private final boolean ordered;

    private NetworkHint(boolean directWrite, boolean ordered) {
        this.directWrite = directWrite;
        this.ordered = ordered;
    }

    public boolean isDirectWrite() {
        return directWrite;
    }

    public boolean isOrdered() {
        return ordered;
    }
}
