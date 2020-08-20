package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

import java.util.UUID;

public class ClientSpectatePacket extends ClientPlayPacket {

    public UUID targetUuid;

    @Override
    public void read(BinaryReader reader) {
        // TODO reader uuid
    }
}
