package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.play.*;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x05, ClientSettingsPacket.class);
        register(0x0B, ClientPluginMessagePacket.class);
        register(0x11, ClientPlayerPositionPacket.class);
        register(0x12, ClientPlayerPositionAndLookPacket.class);
        register(0x00, ClientTeleportConfirmPacket.class);
    }

}
