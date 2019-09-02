package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientHeldItemChangePacket extends ClientPlayPacket {

    public short slot;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readShort(value -> {
            slot = value;
            callback.run();
        });
    }
}
