package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.play.ClientPlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.client.play.ClientPluginMessagePacket;
import fr.themode.minestom.net.packet.client.play.ClientSettingsPacket;
import fr.themode.minestom.net.packet.client.play.ClientTeleportConfirmPacket;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x05, ClientSettingsPacket.class);
        register(0x0B, ClientPluginMessagePacket.class);
        register(0x12, ClientPlayerPositionAndLookPacket.class);
        register(0x00, ClientTeleportConfirmPacket.class);
    }

}
