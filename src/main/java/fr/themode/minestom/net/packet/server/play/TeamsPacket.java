package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class TeamsPacket implements ServerPacket {

    public String teamName;
    public Action action;

    public String teamDisplayName;
    public byte friendlyFlags;
    public String nameTagVisibility;
    public String collisionRule;
    public int teamColor;
    public String teamPrefix;
    public String teamSuffix;
    public int entityCount;
    public String[] entities;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(teamName);
        writer.writeByte((byte) action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                writer.writeSizedString(teamDisplayName);
                writer.writeByte(friendlyFlags);
                writer.writeSizedString(nameTagVisibility);
                writer.writeSizedString(collisionRule);
                writer.writeVarInt(teamColor);
                writer.writeSizedString(teamPrefix);
                writer.writeSizedString(teamSuffix);
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            writer.writeStringArray(entities);
        }

    }

    @Override
    public int getId() {
        return 0x4B;
    }

    public enum Action {
        CREATE_TEAM,
        REMOVE_TEAM,
        UPDATE_TEAM_INFO,
        ADD_PLAYERS_TEAM,
        REMOVE_PLAYERS_TEAM;
    }

}
