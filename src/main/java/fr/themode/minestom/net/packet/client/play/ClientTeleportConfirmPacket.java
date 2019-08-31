package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientTeleportConfirmPacket extends ClientPlayPacket {

    public int teleportId;

    @Override
    public void read(PacketReader reader) {
        this.teleportId = reader.readVarInt();
    }
}
