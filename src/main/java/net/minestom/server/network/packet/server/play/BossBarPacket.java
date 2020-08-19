package net.minestom.server.network.packet.server.play;

import net.minestom.server.bossbar.BarColor;
import net.minestom.server.bossbar.BarDivision;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

import java.util.UUID;

public class BossBarPacket implements ServerPacket {

    public UUID uuid;
    public Action action;

    public ColoredText title;
    public float health;
    public BarColor color;
    public BarDivision division;
    public byte flags;


    @Override
    public void write(BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case ADD:
                writer.writeSizedString(title.toString());
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
                writer.writeSizedString(title.toString());
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
        return ServerPacketIdentifier.BOSS_BAR;
    }

    public enum Action {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS
    }

}
