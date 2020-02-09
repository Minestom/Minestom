package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class TeamsPacket implements ServerPacket {

    public String teamName;
    public Action action;

    public String teamDisplayName;
    public byte friendlyFlags;
    public NameTagVisibility nameTagVisibility;
    public CollisionRule collisionRule;
    public int teamColor;
    public String teamPrefix;
    public String teamSuffix;
    public String[] entities;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(teamName);
        writer.writeByte((byte) action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                writer.writeSizedString(Chat.legacyTextString(teamDisplayName));
                writer.writeByte(friendlyFlags);
                writer.writeSizedString(nameTagVisibility.getIdentifier());
                writer.writeSizedString(collisionRule.getIdentifier());
                writer.writeVarInt(teamColor);
                writer.writeSizedString(Chat.legacyTextString(teamPrefix));
                writer.writeSizedString(Chat.legacyTextString(teamSuffix));
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
        return 0x4C;
    }

    public enum Action {
        CREATE_TEAM,
        REMOVE_TEAM,
        UPDATE_TEAM_INFO,
        ADD_PLAYERS_TEAM,
        REMOVE_PLAYERS_TEAM;
    }

    public enum NameTagVisibility {
        ALWAYS("always"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        NEVER("never");

        private String identifier;

        NameTagVisibility(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public enum CollisionRule {
        ALWAYS("always"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam"),
        NEVER("never");

        private String identifier;

        CollisionRule(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

}
