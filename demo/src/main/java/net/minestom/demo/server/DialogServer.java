package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.chat.ChatFeature;
import net.minestom.demo.feature.dialog.DialogFeature;

/** Chat-triggered dialog. */
public final class DialogServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ChatFeature(),
                new DialogFeature()
        ).start();
    }
}
