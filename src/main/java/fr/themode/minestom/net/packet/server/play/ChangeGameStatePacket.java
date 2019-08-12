package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class ChangeGameStatePacket implements ServerPacket {

    public Reason reason;
    public float value;

    @Override
    public void write(Buffer buffer) {
        buffer.putByte((byte) reason.ordinal());
        buffer.putFloat(value);
    }

    @Override
    public int getId() {
        return 0x1E;
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
        PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE;
    }

}
