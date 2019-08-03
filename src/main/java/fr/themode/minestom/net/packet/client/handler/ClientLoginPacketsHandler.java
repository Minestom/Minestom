package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.login.LoginStartPacket;

public class ClientLoginPacketsHandler extends ClientPacketsHandler {

    public ClientLoginPacketsHandler() {
        register(0, LoginStartPacket.class);
    }

}
