package net.minestom.server.scoreboard;

import net.minestom.server.chat.ColoredText;
import net.minestom.server.network.packet.server.play.TeamsPacket;

public class SidebarTeam {

    private String teamName;
    private ColoredText prefix, suffix;
    private String entityName;

    private ColoredText teamDisplayName = ColoredText.of("displaynametest");
    private byte friendlyFlags = 0x00;
    private TeamsPacket.NameTagVisibility nameTagVisibility = TeamsPacket.NameTagVisibility.NEVER;
    private TeamsPacket.CollisionRule collisionRule = TeamsPacket.CollisionRule.NEVER;
    private int teamColor = 2;


    protected SidebarTeam(String teamName, ColoredText prefix, ColoredText suffix, String entityName) {
        this.teamName = teamName;
        this.prefix = prefix;
        this.suffix = suffix;
        this.entityName = entityName;
    }

    protected TeamsPacket getCreationPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.CREATE_TEAM;
        teamsPacket.teamDisplayName = teamDisplayName.toString();
        teamsPacket.friendlyFlags = friendlyFlags;
        teamsPacket.nameTagVisibility = nameTagVisibility;
        teamsPacket.collisionRule = collisionRule;
        teamsPacket.teamColor = teamColor;
        teamsPacket.teamPrefix = prefix.toString();
        teamsPacket.teamSuffix = suffix.toString();
        teamsPacket.entities = new String[]{entityName};
        return teamsPacket;
    }

    protected TeamsPacket getDestructionPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        return teamsPacket;
    }

    protected TeamsPacket updatePrefix(ColoredText prefix) {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        teamsPacket.teamDisplayName = teamDisplayName.toString();
        teamsPacket.friendlyFlags = friendlyFlags;
        teamsPacket.nameTagVisibility = nameTagVisibility;
        teamsPacket.collisionRule = collisionRule;
        teamsPacket.teamColor = teamColor;
        teamsPacket.teamPrefix = prefix.toString();
        teamsPacket.teamSuffix = suffix.toString();
        return teamsPacket;
    }

    protected String getEntityName() {
        return entityName;
    }

    protected ColoredText getPrefix() {
        return prefix;
    }

    protected void refreshPrefix(ColoredText prefix) {
        this.prefix = prefix;
    }
}
