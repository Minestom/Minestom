package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerAbilitiesPacket implements ClientPlayPacket {

    private byte flags;
    private float flyingSpeed;
    private float walkingSpeed;

    @Override
    public void process(Player player) {

    }

    @Override
    public void read(Buffer buffer) {
        this.flags = buffer.getByte();
        this.flyingSpeed = buffer.getFloat();
        this.walkingSpeed = buffer.getFloat();
    }
}
