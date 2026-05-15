package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.transfer.TransferFeature;

/**
 * Cross-server transfer / cookie showcase. Run two copies on different
 * ports and use {@code /transfer <host> <port>} to hop between them;
 * {@code /cookie} round-trips a persistent payload through the client.
 */
public final class TransferServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new TransferFeature()
        ).start();
    }
}
