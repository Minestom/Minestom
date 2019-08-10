package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPositionPacket implements ClientPlayPacket {

    public double x, y, z;
    public boolean onGround;

    @Override
    public void process(Player player) {
        player.refreshPosition(x, y, z);
        player.refreshOnGround(onGround);
    }

    @Override
    public void read(Buffer buffer) {
        this.x = buffer.getDouble();
        this.y = buffer.getDouble();
        this.z = buffer.getDouble();
        this.onGround = buffer.getBoolean();
    }
}
