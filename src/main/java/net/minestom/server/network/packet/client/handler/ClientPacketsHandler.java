package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.handler.PacketsHandler;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @see net.minestom.server.network.packet.handler.PacketsHandler
 */
public class ClientPacketsHandler<Type extends ClientPacket> extends PacketsHandler<Type> {

    public ClientPacketsHandler() {
        super(0x30);
    }
}
