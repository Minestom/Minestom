package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.core.ServerListPingFeature;
import net.minestom.demo.feature.networking.NetworkingFeature;
import net.minestom.demo.feature.transfer.TransferFeature;

/**
 * Protocol-layer showcase: server-list ping (MOTD, favicon, sample
 * lines), server links + custom report details on spawn, the
 * {@code /config} command, and transfer/cookie commands.
 */
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
