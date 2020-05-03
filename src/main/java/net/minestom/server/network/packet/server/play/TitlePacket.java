package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.Chat;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class TitlePacket implements ServerPacket {

    public Action action;

    public String titleText;

    public String subtitleText;

    public String actionBarText;

    public int fadeIn;
    public int stay;
    public int fadeOut;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case SET_TITLE:
                writer.writeSizedString(titleText);
                break;
            case SET_SUBTITLE:
                writer.writeSizedString(subtitleText);
                break;
            case SET_ACTION_BAR:
                writer.writeSizedString(actionBarText);
                break;
            case SET_TIMES_AND_DISPLAY:
                writer.writeInt(fadeIn);
                writer.writeInt(stay);
                writer.writeInt(fadeOut);
                break;
            case HIDE:
            case RESET:
                break;
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TITLE;
    }

    public enum Action {
        SET_TITLE,
        SET_SUBTITLE,
        SET_ACTION_BAR,
        SET_TIMES_AND_DISPLAY,
        HIDE,
        RESET
    }

}
