package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientPlayerPositionPacket extends ClientPlayPacket {

    public double x, y, z;
    public boolean onGround;

    @Override
    public void read(BinaryReader reader) {
        this.x = reader.readDouble();
        this.y = reader.readDouble();
        this.z = reader.readDouble();

        this.onGround = reader.readBoolean();
    }
}
