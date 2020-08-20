package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientEntityActionPacket extends ClientPlayPacket {

    public int playerId;
    public Action action;
    public int horseJumpBoost;

    @Override
    public void read(BinaryReader reader) {
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
        START_FLYING_ELYTRA
    }

}
