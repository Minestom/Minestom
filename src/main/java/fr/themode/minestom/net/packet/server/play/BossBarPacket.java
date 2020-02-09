package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.bossbar.BarColor;
import fr.themode.minestom.bossbar.BarDivision;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
    public void write(PacketWriter writer) {
        writer.writeUuid(uuid);
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case ADD:
                writer.writeSizedString(Chat.legacyTextString(title));
                writer.writeFloat(health);
                writer.writeVarInt(color.ordinal());
                writer.writeVarInt(division.ordinal());
                writer.writeByte(flags);
                break;
            case REMOVE:

                break;
            case UPDATE_HEALTH:
                writer.writeFloat(health);
                break;
            case UPDATE_TITLE:
                writer.writeSizedString(Chat.legacyTextString(title));
                break;
            case UPDATE_STYLE:
                writer.writeVarInt(color.ordinal());
                writer.writeVarInt(division.ordinal());
                break;
            case UPDATE_FLAGS:
                writer.writeByte(flags);
                break;
        }
    }

    @Override
    public int getId() {
        return 0x0D;
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
