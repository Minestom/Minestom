package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientInteractEntityPacket extends ClientPlayPacket {

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

        switch (type) {
            case INTERACT:
                this.hand = Player.Hand.values()[reader.readVarInt()];
                break;
            case ATTACK:
                break;
            case INTERACT_AT:
                this.x = reader.readFloat();
                this.y = reader.readFloat();
                this.z = reader.readFloat();
                this.hand = Player.Hand.values()[reader.readVarInt()];
                break;

        }
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
