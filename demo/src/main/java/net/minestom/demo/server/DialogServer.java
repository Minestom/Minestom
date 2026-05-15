package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.feature.chat.ChatFeature;
import net.minestom.demo.feature.dialog.DialogFeature;

/**
 * Dialog-only launcher: chat triggers the demo {@code MultiAction}
 * dialog, custom-click payloads are logged. Ships the chat feature too
 * so the chat → dialog trigger is wired.
 */
public final class DialogServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ChatFeature(),
                new DialogFeature()
        ).start();
    }
}
