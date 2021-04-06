package net.minestom.server.network.packet.server.handler;

import net.minestom.server.network.packet.handler.PacketsHandler;
import net.minestom.server.network.packet.server.ServerPacket;

/**
 * @see net.minestom.server.network.packet.handler.PacketsHandler
 */
public class ServerPacketsHandler extends PacketsHandler<ServerPacket> {

    public ServerPacketsHandler() {
        super(0x60);
    }
}
