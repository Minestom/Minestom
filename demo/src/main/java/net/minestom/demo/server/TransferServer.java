package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.transfer.TransferFeature;

/** {@code /transfer} and {@code /cookie}. Pair with a second instance on another port. */
public final class TransferServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new TransferFeature()
        ).start();
    }
}
