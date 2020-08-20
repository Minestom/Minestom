package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientTeleportConfirmPacket extends ClientPlayPacket {

    public int teleportId;

    @Override
    public void read(BinaryReader reader) {
        this.teleportId = reader.readVarInt();
    }
}
