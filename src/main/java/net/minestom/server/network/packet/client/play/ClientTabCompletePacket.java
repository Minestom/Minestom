package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientTabCompletePacket extends ClientPlayPacket {

    public int transactionId;
    public String text;

    @Override
    public void read(PacketReader reader) {
        this.transactionId = reader.readVarInt();
        this.text = reader.readSizedString();
    }
}
