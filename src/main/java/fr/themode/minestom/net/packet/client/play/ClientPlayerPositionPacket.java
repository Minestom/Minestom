package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPositionPacket extends ClientPlayPacket {

    public double x, y, z;
    public boolean onGround;

    @Override
    public void read(PacketReader reader) {
        this.x = reader.readDouble();
        this.y = reader.readDouble();
        this.z = reader.readDouble();
        this.onGround = reader.readBoolean();
    }
}
