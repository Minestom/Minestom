package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.utils.Utils;

public class ClientUseEntityPacket implements ClientPlayPacket {

    private int target;
    private Type type;
    private float x;
    private float y;
    private float z;
    private Player.Hand hand;

    @Override
    public void process(Player player) {

    }

    @Override
    public void read(Buffer buffer) {
        this.target = Utils.readVarInt(buffer);
        this.type = Type.values()[Utils.readVarInt(buffer)];
        if (this.type == Type.INTERACT_AT) {
            this.x = buffer.getFloat();
            this.y = buffer.getFloat();
            this.z = buffer.getFloat();
        }
        if (type == Type.INTERACT || type == Type.INTERACT_AT)
            this.hand = Player.Hand.values()[Utils.readVarInt(buffer)];
    }

    public static enum Type {

        INTERACT,
        ATTACK,
        INTERACT_AT;
    }
}
