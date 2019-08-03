package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class TestPacket implements ClientPlayPacket {

    @Override
    public void process(Player player) {

    }

    @Override
    public void read(Buffer buffer) {
        System.out.println("Hey c'est moi");
    }
}
