package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class TitlePacket implements ServerPacket {

    public Action action;

    public JsonMessage titleText; // Only text

    public JsonMessage subtitleText; // Only text

    public JsonMessage actionBarText; // Only text

    public int fadeIn;
    public int stay;
    public int fadeOut;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());

        switch (action) {
            case SET_TITLE:
                writer.writeSizedString(titleText.toString());
                break;
            case SET_SUBTITLE:
                writer.writeSizedString(subtitleText.toString());
                break;
            case SET_ACTION_BAR:
                writer.writeSizedString(actionBarText.toString());
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
