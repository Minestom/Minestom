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
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(value -> targetId = value);
        reader.readVarInt(value -> {
            type = Type.values()[value];
            if (type == Type.ATTACK)
                callback.run();
        });

        if (this.type == Type.INTERACT_AT) {
            reader.readFloat(value -> x = value);
            reader.readFloat(value -> y = value);
            reader.readFloat(value -> {
                z = value;
            });
        }
        if (type == Type.INTERACT || type == Type.INTERACT_AT)
            reader.readVarInt(value -> {
                hand = Player.Hand.values()[value];
                callback.run();
            });
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT;
    }
}
