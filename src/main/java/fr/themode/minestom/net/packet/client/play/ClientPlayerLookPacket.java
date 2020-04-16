package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerLookPacket extends ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(PacketReader reader) {
        this.yaw = reader.readFloat();
        this.pitch = reader.readFloat();
        this.onGround = reader.readBoolean();
    }
}
