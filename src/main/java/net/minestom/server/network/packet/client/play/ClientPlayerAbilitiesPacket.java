package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientPlayerAbilitiesPacket extends ClientPlayPacket {

    public byte flags;

    @Override
    public void read(BinaryReader reader) {
        this.flags = reader.readByte();
    }
}
