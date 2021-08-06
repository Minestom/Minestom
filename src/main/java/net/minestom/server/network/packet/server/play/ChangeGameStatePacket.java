package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ChangeGameStatePacket implements ServerPacket {

    public Reason reason;
    public float value;

    public ChangeGameStatePacket() {
        reason = Reason.NO_RESPAWN_BLOCK;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte((byte) reason.ordinal());
        writer.writeFloat(value);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        reason = Reason.values()[reader.readByte()];
        value = reader.readFloat();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHANGE_GAME_STATE;
    }

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
        ENABLE_RESPAWN_SCREEN
    }

}
