package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.play.*;

public class ClientPlayPacketsHandler extends ClientPacketsHandler {

    public ClientPlayPacketsHandler() {
        register(0x05, ClientSettingsPacket.class);
        register(0x0B, ClientPluginMessagePacket.class);
        register(0x11, ClientPlayerPositionPacket.class);
        register(0x12, ClientPlayerPositionAndLookPacket.class);
        register(0x00, ClientTeleportConfirmPacket.class);
        register(0x0F, ClientKeepAlivePacket.class);
        register(0x19, ClientPlayerAbilitiesPacket.class);
        register(0x13, ClientPlayerLookPacket.class);
        register(0x14, ClientPlayerPacket.class);
        register(0x2A, ClientAnimationPacket.class);
        register(0x1B, ClientEntityActionPacket.class);
        register(0x0E, ClientUseEntityPacket.class);
        register(0x03, ClientChatMessagePacket.class);
    }
}
