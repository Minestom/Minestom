package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientPlayerPacket extends ClientPlayPacket {

    public boolean onGround;

    @Override
    public void read(PacketReader reader) {
        this.onGround = reader.readBoolean();
    }
}
