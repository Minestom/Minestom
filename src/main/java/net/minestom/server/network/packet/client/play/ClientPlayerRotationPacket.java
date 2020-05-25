package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientPlayerRotationPacket extends ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(PacketReader reader) {
        this.yaw = reader.readFloat();
        this.pitch = reader.readFloat();
        this.onGround = reader.readBoolean();
    }
}
