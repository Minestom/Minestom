package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientTeleportConfirmPacket extends ClientPlayPacket {

    public int teleportId;

    @Override
    public void read(PacketReader reader) {
        this.teleportId = reader.readVarInt();
    }
}
