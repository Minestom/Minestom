package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

import java.util.UUID;

public class ClientSpectatePacket extends ClientPlayPacket {

    public UUID targetUuid;

    @Override
    public void read(PacketReader reader) {
        // TODO reader uuid
    }
}
