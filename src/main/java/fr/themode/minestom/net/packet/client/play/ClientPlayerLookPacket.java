package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerLookPacket implements ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void process(Player player) {
        player.refreshView(yaw, pitch);
        player.refreshOnGround(onGround);
    }

    @Override
    public void read(Buffer buffer) {
        this.yaw = buffer.getFloat();
        this.pitch = buffer.getFloat();
        this.onGround = buffer.getBoolean();
    }
}
