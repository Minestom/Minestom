package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class ChangeGameStatePacket implements ServerPacket {

    public Reason reason;
    public float value;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte((byte) reason.ordinal());
        writer.writeFloat(value);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHANGE_GAME_STATE;
    }

    public enum Reason {
        INVALID_BED,
        END_RAINING,
        BEGIN_RAINING,
        CHANGE_GAMEMODE,
        EXIT_END,
        DEMO_MESSAGE,
        ARROW_HITTING_PLAYER,
        FADE_VALUE,
        FADE_TIME,
        PLAY_PUFFERFISH_STING_SOUND,
        PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE,
        ENABLE_RESPAWN_SCREEN;
    }

}
