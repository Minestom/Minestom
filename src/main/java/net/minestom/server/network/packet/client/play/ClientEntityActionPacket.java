package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.Enum;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientEntityActionPacket(int playerId, @NotNull Action action,
                                       int horseJumpBoost) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientEntityActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientEntityActionPacket::playerId,
            Enum(Action.class), ClientEntityActionPacket::action,
            VAR_INT, ClientEntityActionPacket::horseJumpBoost,
            ClientEntityActionPacket::new);

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
