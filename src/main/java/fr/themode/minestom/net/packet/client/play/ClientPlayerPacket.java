package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPacket implements ClientPlayPacket {

    public boolean onGround;

    @Override
    public void process(Player player) {
        player.refreshOnGround(onGround);
    }

    @Override
    public void read(Buffer buffer) {
        this.onGround = buffer.getBoolean();
    }
}
