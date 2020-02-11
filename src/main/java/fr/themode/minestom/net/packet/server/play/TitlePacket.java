package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
                writer.writeSizedString(Chat.legacyTextString(titleText));
                break;
            case SET_SUBTITLE:
                writer.writeSizedString(Chat.legacyTextString(subtitleText));
                break;
            case SET_ACTION_BAR:
                writer.writeSizedString(Chat.legacyTextString(actionBarText));
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
        RESET;
    }

}
