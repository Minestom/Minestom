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

            switch (type) {

                case ATTACK:
                    callback.run();
                    break;

                case INTERACT:
                    reader.readVarInt(v2 -> {
                        hand = Player.Hand.values()[v2];
                        callback.run();
                    });
                    break;

                case INTERACT_AT:
                    reader.readFloat(vX -> x = vX);
                    reader.readFloat(vY -> y = vY);
                    reader.readFloat(vZ -> z = vZ);
                    reader.readVarInt(v2 -> {
                        hand = Player.Hand.values()[v2];
                        callback.run();
                    });
                    break;

            }
        });
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT;
    }
}
