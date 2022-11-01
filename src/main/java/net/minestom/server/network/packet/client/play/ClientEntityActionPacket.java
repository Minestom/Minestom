package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientEntityActionPacket(int playerId, @NotNull Action action,
                                       int horseJumpBoost) implements ClientPacket {
    public ClientEntityActionPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.readEnum(Action.class),
                reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, playerId);
        writer.writeEnum(Action.class, action);
        writer.write(VAR_INT, horseJumpBoost);
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
