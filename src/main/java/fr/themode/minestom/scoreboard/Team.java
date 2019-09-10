package fr.themode.minestom.scoreboard;

import fr.themode.minestom.net.packet.server.play.TeamsPacket;

public class Team {

    private String teamName;
    private String prefix, suffix;
    private String entityName;

    private String teamDisplayName = "displaynametest";
    private byte friendlyFlags = 0x00;
    private String nameTagVisibility = "never";
    private String collisionRule = "never";
    private int teamColor = 2;


    protected Team(String teamName, String prefix, String suffix, String entityName) {
        this.teamName = teamName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.entityName = entityName;
    }

    protected TeamsPacket getCreationPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.CREATE_TEAM;
        teamsPacket.teamDisplayName = teamDisplayName;
        teamsPacket.friendlyFlags = friendlyFlags;
        teamsPacket.nameTagVisibility = nameTagVisibility;
        teamsPacket.collisionRule = collisionRule;
        teamsPacket.teamColor = teamColor;
        teamsPacket.teamPrefix = prefix;
        teamsPacket.teamSuffix = suffix;
        teamsPacket.entities = new String[]{entityName};
        return teamsPacket;
    }

    protected TeamsPacket getDestructionPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        return teamsPacket;
    }

    protected TeamsPacket updatePrefix(String prefix) {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        teamsPacket.teamDisplayName = teamDisplayName;
        teamsPacket.friendlyFlags = friendlyFlags;
        teamsPacket.nameTagVisibility = nameTagVisibility;
        teamsPacket.collisionRule = collisionRule;
        teamsPacket.teamColor = teamColor;
        teamsPacket.teamPrefix = prefix;
        teamsPacket.teamSuffix = suffix;
        return teamsPacket;
    }

    protected String getEntityName() {
        return entityName;
    }

    protected String getPrefix() {
        return prefix;
    }

    protected void refreshPrefix(String prefix) {
        this.prefix = prefix;
    }
}
