package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientKeepAlivePacket extends ClientPlayPacket {

    public long id;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readLong(value -> {
            id = value;
            callback.run();
        });
    }
}
