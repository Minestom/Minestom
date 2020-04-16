package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

import java.util.UUID;

public class ClientSpectatePacket extends ClientPlayPacket {

    public UUID targetUuid;

    @Override
    public void read(PacketReader reader) {
        // TODO reader uuid
    }
}
