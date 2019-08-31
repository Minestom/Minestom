package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientEntityActionPacket extends ClientPlayPacket {

    public int playerId;
    public Action action;
    public int horseJumpBoost;

    @Override
    public void read(PacketReader reader) {
        this.playerId = reader.readVarInt();
        this.action = Action.values()[reader.readVarInt()];
        this.horseJumpBoost = reader.readVarInt();
    }

    public enum Action {
        START_SNEAKING,
        STOP_SNEAKING,
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_JUMP_HORSE,
        STOP_JUMP_HORSE,
        OPEN_HORSE_INVENTORY,
        START_FLYING_ELYTRA;
    }

}
