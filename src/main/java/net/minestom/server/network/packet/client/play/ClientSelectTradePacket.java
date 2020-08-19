package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientSelectTradePacket extends ClientPlayPacket {

    public int selectedSlot;

    @Override
    public void read(BinaryReader reader) {
        this.selectedSlot = reader.readVarInt();
    }
}
