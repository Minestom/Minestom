package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientPickItemPacket extends ClientPlayPacket {

    public int slotToUse;

    @Override
    public void read(PacketReader reader) {
        this.slotToUse = reader.readVarInt();
    }
}
