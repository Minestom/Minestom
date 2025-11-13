package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ChangeGameStatePacket(Reason reason, float value) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChangeGameStatePacket> SERIALIZER = NetworkBufferTemplate.template(
            Reason.NETWORK_TYPE, ChangeGameStatePacket::reason,
            FLOAT, ChangeGameStatePacket::value,
            ChangeGameStatePacket::new
    );

    public enum Reason {
        NO_RESPAWN_BLOCK,
        END_RAINING,
        BEGIN_RAINING,
        CHANGE_GAMEMODE,
        WIN_GAME,
        DEMO_EVENT,
        ARROW_HIT_PLAYER,
        RAIN_LEVEL_CHANGE,
        THUNDER_LEVEL_CHANGE,
        PLAY_PUFFERFISH_STING_SOUND,
        PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE,
        ENABLE_RESPAWN_SCREEN,
        LIMITED_CRAFTING,
        LEVEL_CHUNKS_LOAD_START;

        public static final NetworkBuffer.Type<Reason> NETWORK_TYPE = NetworkBuffer.ByteEnum(Reason.class);
    }
}
