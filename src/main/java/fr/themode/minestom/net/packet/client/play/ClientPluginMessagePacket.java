package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    public String identifier;
    public byte[] data;

    @Override
    public void read(PacketReader reader) {
        this.identifier = reader.readSizedString();
        this.data = reader.getRemainingBytes();
    }
}
