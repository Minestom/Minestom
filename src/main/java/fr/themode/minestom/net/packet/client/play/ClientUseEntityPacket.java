package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientUseEntityPacket extends ClientPlayPacket {

    public int targetId;
    public Type type;
    public float x;
    public float y;
    public float z;
    public Player.Hand hand;

    @Override
    public void read(PacketReader reader) {
        this.targetId = reader.readVarInt();
        this.type = Type.values()[reader.readVarInt()];
        if (this.type == Type.INTERACT_AT) {
            this.x = reader.readFloat();
            this.y = reader.readFloat();
            this.z = reader.readFloat();
        }
        if (type == Type.INTERACT || type == Type.INTERACT_AT)
            this.hand = Player.Hand.values()[reader.readVarInt()];
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT;
    }
}
