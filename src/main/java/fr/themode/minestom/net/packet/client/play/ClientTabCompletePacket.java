package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientTabCompletePacket extends ClientPlayPacket {

    public int transactionId;
    public String text;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(i -> transactionId = i);
        reader.readSizedString((string, length) -> {
            text = string;
            callback.run();
        });
    }
}
