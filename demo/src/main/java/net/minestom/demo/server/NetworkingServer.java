package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.core.ServerListPingFeature;
import net.minestom.demo.feature.networking.NetworkingFeature;
import net.minestom.demo.feature.transfer.TransferFeature;

/** Server-list ping, server links/reports, config phase, transfer. */
public final class NetworkingServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ServerListPingFeature(),
                new NetworkingFeature(),
                new TransferFeature()
        ).start();
    }
}
