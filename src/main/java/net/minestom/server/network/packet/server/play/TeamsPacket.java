package net.minestom.server.network.packet.server.play;

import net.kyori.text.Component;
import net.minestom.server.chat.Chat;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class TeamsPacket implements ServerPacket {

    public String teamName;
    public Action action;

    public Component teamDisplayName;
    public byte friendlyFlags;
    public NameTagVisibility nameTagVisibility;
    public CollisionRule collisionRule;
    public int teamColor;
    public Component teamPrefix;
    public Component teamSuffix;
    public String[] entities;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(teamName);
        writer.writeByte((byte) action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                writer.writeSizedString(Chat.toJsonString(teamDisplayName));
                writer.writeByte(friendlyFlags);
                writer.writeSizedString(nameTagVisibility.getIdentifier());
                writer.writeSizedString(collisionRule.getIdentifier());
                writer.writeVarInt(teamColor);
                writer.writeSizedString(Chat.toJsonString(teamPrefix));
                writer.writeSizedString(Chat.toJsonString(teamSuffix));
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
        return ServerPacketIdentifier.TEAMS;
    }

    public enum Action {
        CREATE_TEAM,
        REMOVE_TEAM,
        UPDATE_TEAM_INFO,
        ADD_PLAYERS_TEAM,
        REMOVE_PLAYERS_TEAM
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
