package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    public String channel;
    public byte[] data;

    @Override
    public void read(BinaryReader reader) {
        this.channel = reader.readSizedString();
        this.data = reader.getRemainingBytes();
    }
}
