package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerLookPacket extends ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(Buffer buffer) {
        this.yaw = buffer.getFloat();
        this.pitch = buffer.getFloat();
        this.onGround = buffer.getBoolean();
    }
}
