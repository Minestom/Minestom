package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientEntityActionPacket(int playerId, @NotNull Action action,
                                       int horseJumpBoost) implements ClientPacket {
    public ClientEntityActionPacket(BinaryReader reader) {
        this(reader.readVarInt(), Action.values()[reader.readVarInt()],
                reader.readVarInt());
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
