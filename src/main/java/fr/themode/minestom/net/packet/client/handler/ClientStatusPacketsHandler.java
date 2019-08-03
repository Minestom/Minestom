package fr.themode.minestom.net.packet.client.handler;

import fr.themode.minestom.net.packet.client.status.PingPacket;
import fr.themode.minestom.net.packet.client.status.StatusRequestPacket;

public class ClientStatusPacketsHandler extends ClientPacketsHandler {

    public ClientStatusPacketsHandler() {
        register(0x00, StatusRequestPacket.class);
        register(0x01, PingPacket.class);
    }

}
