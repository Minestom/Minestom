package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.bossbar.BarColor;
import fr.themode.minestom.bossbar.BarDivision;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

import java.util.UUID;

public class BossBarPacket implements ServerPacket {

    public UUID uuid;
    public Action action;

    public String title;
    public float health;
    public BarColor color;
    public BarDivision division;
    public byte flags;


    @Override
    public void write(Buffer buffer) {
        Utils.writeUuid(buffer, uuid);
        Utils.writeVarInt(buffer, action.ordinal());

        switch (action) {
            case ADD:
                Utils.writeString(buffer, title);
                buffer.putFloat(health);
                Utils.writeVarInt(buffer, color.ordinal());
                Utils.writeVarInt(buffer, division.ordinal());
                buffer.putByte(flags);
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                buffer.putFloat(health);
                break;
            case UPDATE_TITLE:
                Utils.writeString(buffer, title);
                break;
            case UPDATE_STYLE:
                Utils.writeVarInt(buffer, color.ordinal());
                Utils.writeVarInt(buffer, division.ordinal());
                break;
            case UPDATE_FLAGS:
                buffer.putByte(flags);
                break;
        }
    }

    @Override
    public int getId() {
        return 0x0C;
    }

    public enum Action {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS;
    }

}
