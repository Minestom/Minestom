package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    private String identifier;
    private byte[] data;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readSizedString((s, l) -> {
            identifier = s;
            reader.getRemainingBytes(l, bytes -> {
                data = bytes;
                callback.run();
            });
        });
    }
}
