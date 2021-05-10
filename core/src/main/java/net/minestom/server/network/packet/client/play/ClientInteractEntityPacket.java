package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientInteractEntityPacket extends ClientPlayPacket {

    public int targetId;
    public Type type = Type.INTERACT;
    public float x;
    public float y;
    public float z;
    public Player.Hand hand = Player.Hand.MAIN;
    public boolean sneaking;

    @Override
    public void read(@NotNull BinaryReader reader) {
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

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(targetId);
        writer.writeVarInt(type.ordinal());

        switch (type) {
            case INTERACT:
                writer.writeVarInt(hand.ordinal());
                break;
            case ATTACK:
                break;
            case INTERACT_AT:
                writer.writeFloat(x);
                writer.writeFloat(y);
                writer.writeFloat(z);
                writer.writeVarInt(hand.ordinal());
                break;
        }
        writer.writeBoolean(sneaking);
    }

    public enum Type {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
