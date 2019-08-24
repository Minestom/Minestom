package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

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
    public void write(Buffer buffer) {
        Utils.writeString(buffer, teamName);
        buffer.putByte((byte) action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                Utils.writeString(buffer, teamDisplayName);
                buffer.putByte(friendlyFlags);
                Utils.writeString(buffer, nameTagVisibility);
                Utils.writeString(buffer, collisionRule);
                Utils.writeVarInt(buffer, teamColor);
                Utils.writeString(buffer, teamPrefix);
                Utils.writeString(buffer, teamSuffix);
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            if (entities == null) {
                Utils.writeVarInt(buffer, 0);
                return;
            }

            Utils.writeVarInt(buffer, entities.length);
            for (String entity : entities) {
                Utils.writeString(buffer, entity);
            }
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
