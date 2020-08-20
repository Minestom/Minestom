package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientPickItemPacket extends ClientPlayPacket {

    public int slotToUse;

    @Override
    public void read(BinaryReader reader) {
        this.slotToUse = reader.readVarInt();
    }
}
