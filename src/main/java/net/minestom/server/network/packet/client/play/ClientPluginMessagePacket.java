package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    public String channel;
    public byte[] data;

    @Override
    public void read(PacketReader reader) {
        this.channel = reader.readSizedString();
        this.data = reader.getRemainingBytes();
    }
}
