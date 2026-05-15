package net.minestom.demo.server;

import net.minestom.demo.core.DemoServer;
import net.minestom.demo.core.LobbyFeature;
import net.minestom.demo.core.ServerListPingFeature;
import net.minestom.demo.feature.chat.ChatFeature;
import net.minestom.demo.feature.dialog.DialogFeature;
import net.minestom.demo.feature.input.InputFeature;

/**
 * Chat-side showcase: text components, sidebar, notifications, dialogs,
 * and the keybind action-bar HUD.
 */
public final class ChatServer {

    static void main(String[] args) {
        DemoServer.create().features(
                new LobbyFeature(),
                new ServerListPingFeature(),
                new ChatFeature(),
                new DialogFeature(),
                new InputFeature()
        ).start();
    }
}
