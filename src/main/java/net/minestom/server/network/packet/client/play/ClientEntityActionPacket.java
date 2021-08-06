package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientEntityActionPacket extends ClientPlayPacket {

    public int playerId;
    public Action action = Action.START_SNEAKING;
    public int horseJumpBoost;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.playerId = reader.readVarInt();
        this.action = Action.values()[reader.readVarInt()];
        this.horseJumpBoost = reader.readVarInt();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(playerId);
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(horseJumpBoost);
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
