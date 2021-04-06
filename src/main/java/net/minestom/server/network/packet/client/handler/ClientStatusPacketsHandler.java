package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.status.PingPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;

public class ClientStatusPacketsHandler extends ClientPacketsHandler<ClientPreplayPacket> {

    public ClientStatusPacketsHandler() {
        register(0x00, StatusRequestPacket::new);
        register(0x01, PingPacket::new);
    }

}
