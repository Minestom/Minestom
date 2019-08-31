package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPacket extends ClientPlayPacket {

    public boolean onGround;

    @Override
    public void read(PacketReader reader) {
        this.onGround = reader.readBoolean();
    }
}
