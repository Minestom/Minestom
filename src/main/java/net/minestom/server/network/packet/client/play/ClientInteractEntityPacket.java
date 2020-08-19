package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientInteractEntityPacket extends ClientPlayPacket {

    public int targetId;
    public Type type;
    public float x;
    public float y;
    public float z;
    public Player.Hand hand;
    public boolean sneaking;

    @Override
    public void read(BinaryReader reader) {
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
        this.sneaking = reader.readBoolean();
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
