package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientTabCompletePacket extends ClientPlayPacket {

    public int transactionId;
    public String text;

    @Override
    public void read(BinaryReader reader) {
        this.transactionId = reader.readVarInt();
        this.text = reader.readSizedString();
    }
}
