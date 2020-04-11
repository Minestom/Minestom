package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientNameItemPacket extends ClientPlayPacket {

    public String itemName;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readSizedString((string, length) -> {
            itemName = string;
            callback.run();
        });
    }
}
