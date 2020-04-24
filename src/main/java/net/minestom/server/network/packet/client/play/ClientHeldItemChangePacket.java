package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientHeldItemChangePacket extends ClientPlayPacket {

    public short slot;

    @Override
    public void read(PacketReader reader) {
        this.slot = reader.readShort();
    }
}
