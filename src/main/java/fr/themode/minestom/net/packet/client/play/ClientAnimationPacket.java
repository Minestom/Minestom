package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientAnimationPacket implements ClientPlayPacket {

    public Hand hand;

    @Override
    public void process(Player player) {
    }

    @Override
    public void read(Buffer buffer) {
        this.hand = Hand.values()[Utils.readVarInt(buffer)];
    }

    public enum Hand {
        MAIN,
        OFF
    }
}
