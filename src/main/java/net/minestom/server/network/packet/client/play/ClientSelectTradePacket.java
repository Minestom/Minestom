package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientSelectTradePacket extends ClientPlayPacket {

    public int selectedSlot;

    @Override
    public void read(PacketReader reader) {
        this.selectedSlot = reader.readVarInt();
    }
}
