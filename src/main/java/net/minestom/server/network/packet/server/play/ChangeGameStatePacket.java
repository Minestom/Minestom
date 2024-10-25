package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ChangeGameStatePacket(@NotNull Reason reason, float value) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChangeGameStatePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ChangeGameStatePacket value) {
            buffer.write(BYTE, (byte) value.reason.ordinal());
            buffer.write(FLOAT, value.value);
        }

        @Override
        public ChangeGameStatePacket read(@NotNull NetworkBuffer buffer) {
            return new ChangeGameStatePacket(Reason.values()[buffer.read(BYTE)], buffer.read(FLOAT));
        }
    };

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
        LEVEL_CHUNKS_LOAD_START
    }
}
